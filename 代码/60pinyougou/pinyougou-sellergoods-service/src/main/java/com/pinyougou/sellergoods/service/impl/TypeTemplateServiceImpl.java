package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;  

import com.pinyougou.sellergoods.service.TypeTemplateService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl extends CoreServiceImpl<TbTypeTemplate>  implements TypeTemplateService {


	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper tbSpecificationOptionMapper;

	@Autowired
	public TypeTemplateServiceImpl(TbTypeTemplateMapper typeTemplateMapper) {
		super(typeTemplateMapper, TbTypeTemplate.class);
		this.typeTemplateMapper=typeTemplateMapper;
	}

	
	

	
	@Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbTypeTemplate> all = typeTemplateMapper.selectAll();
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	@Autowired
	private RedisTemplate redisTemplate;
	

	 @Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();

        if(typeTemplate!=null){			
						if(StringUtils.isNotBlank(typeTemplate.getName())){
				criteria.andLike("name","%"+typeTemplate.getName()+"%");
				//criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getSpecIds())){
				criteria.andLike("specIds","%"+typeTemplate.getSpecIds()+"%");
				//criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getBrandIds())){
				criteria.andLike("brandIds","%"+typeTemplate.getBrandIds()+"%");
				//criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getCustomAttributeItems())){
				criteria.andLike("customAttributeItems","%"+typeTemplate.getCustomAttributeItems()+"%");
				//criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
        List<TbTypeTemplate> all = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);

        //集成redis
         //1.查询所有模板的数据
		 List<TbTypeTemplate> all1 = findAll();

		 for (TbTypeTemplate tbTypeTemplate : all1) {
			 List<Map> maps = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
			 //品牌列表
			 redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(),maps);
			 //规格列表
			 List<Map> specList = findSpecList(tbTypeTemplate.getId());
			 redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specList);

		 }

		 //2.存到redis中
        return pageInfo;
    }

	@Override
	public List<Map> findSpecList(Long typeTemplateId) {
        //1.根据主键 查询模板的对象
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(typeTemplateId);
		//2.获取模板的对象中的规格列表String
		String specIds = tbTypeTemplate.getSpecIds();
		//3.因为数据库里对应的字段值类型为Varchar的json对象，所以要转成json数组
		List<Map> maps = JSON.parseArray(specIds, Map.class);
		//4.循环遍历json数组，根据规格的ID 获取规格下的所有选项列表
		for (Map map : maps) {
			Integer id = (Integer) map.get("id");
			TbSpecificationOption tbSpecificationOption = new TbSpecificationOption();

			tbSpecificationOption.setSpecId(Long.valueOf(id));


			List<TbSpecificationOption> optionList = tbSpecificationOptionMapper.select(tbSpecificationOption);
			//拼接成[{"id":27,"text":"网络",options:[{optionName:'移动3G'},{optionName:'移动4G'}]}
			map.put("options",optionList);
		}
		return maps;
	}


}
