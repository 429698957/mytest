package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;

import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import entity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import tk.mybatis.mapper.entity.Example;

import com.pinyougou.sellergoods.service.SpecificationService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl extends CoreServiceImpl<TbSpecification>  implements SpecificationService {

	
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper optionMapper;
	@Autowired
	public SpecificationServiceImpl(TbSpecificationMapper specificationMapper) {
		super(specificationMapper, TbSpecification.class);
		this.specificationMapper=specificationMapper;
	}


	

	
	@Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbSpecification> all = specificationMapper.selectAll();
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize, TbSpecification specification) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();

        if(specification!=null){			
						if(StringUtils.isNotBlank(specification.getSpecName())){
				criteria.andLike("specName","%"+specification.getSpecName()+"%");
				//criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
        List<TbSpecification> all = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Override
    public void add(Specification specification) {
	    // 获取规格
	    TbSpecification tbSpecification = specification.getSpecification();
	    // 插入数据库
         specificationMapper.insert(tbSpecification);
         //获取规格选项集合
        List<TbSpecificationOption> optionList = specification.getOptionList();
       //遍历
        for (TbSpecificationOption specificationOption : optionList) {
              //规格id对应option表里的spec_id
               //因为前段传过来的数据中没有规格ID 因为是自增所以用到主键返回拿到自增的ID
               //使用通用mapper 会自动将新增的主键返回
                specificationOption.setSpecId(tbSpecification.getId());

            optionMapper.insert(specificationOption);
        }

    }

    @Override
    public Specification showOne(Long Id) {
        Specification specification = new Specification();
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(Id);

        specification.setSpecification(tbSpecification);
        //TbSpecificationOption tbSpecificationOption = optionMapper.selectByPrimaryKey(id);

        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("specId", Id);

        List<TbSpecificationOption> tbSpecificationOptions = optionMapper.selectByExample(example);
        specification.setOptionList(tbSpecificationOptions);


        return specification;
    }

    @Override
    public void update(Specification specification) {


        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        TbSpecificationOption option= new TbSpecificationOption();
        option.setSpecId(specification.getSpecification().getId());
        int delete = optionMapper.delete(option);

        List<TbSpecificationOption> optionList = specification.getOptionList();
        for (TbSpecificationOption tbSpecificationOption : optionList) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            optionMapper.insert(tbSpecificationOption);
        }
        //批量插入 要求 主键为ID 并且是自增才可以
        //optionMapper.insertList(optionList);
    }

    @Override
    public void delete(Long[] ids) {
        Example example = new Example(TbSpecification.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        specificationMapper.deleteByExample(example);

        //删除规格关联的规格的选项
        Example exampleOption = new Example(TbSpecificationOption.class);
        exampleOption.createCriteria().andIn("specId", Arrays.asList(ids));
        optionMapper.deleteByExample(exampleOption);
    }

}
