package com.pinyougou.user.service.impl;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;  

import com.pinyougou.user.service.UserService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl extends CoreServiceImpl<TbUser>  implements UserService {

	
	private TbUserMapper userMapper;

	@Autowired
	public UserServiceImpl(TbUserMapper userMapper) {
		super(userMapper, TbUser.class);
		this.userMapper=userMapper;
	}

	
	

	
	@Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbUser> all = userMapper.selectAll();
        PageInfo<TbUser> info = new PageInfo<TbUser>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize, TbUser user) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if(user!=null){			
						if(StringUtils.isNotBlank(user.getUsername())){
				criteria.andLike("username","%"+user.getUsername()+"%");
				//criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(StringUtils.isNotBlank(user.getPassword())){
				criteria.andLike("password","%"+user.getPassword()+"%");
				//criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(StringUtils.isNotBlank(user.getPhone())){
				criteria.andLike("phone","%"+user.getPhone()+"%");
				//criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(StringUtils.isNotBlank(user.getEmail())){
				criteria.andLike("email","%"+user.getEmail()+"%");
				//criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(StringUtils.isNotBlank(user.getSourceType())){
				criteria.andLike("sourceType","%"+user.getSourceType()+"%");
				//criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(StringUtils.isNotBlank(user.getNickName())){
				criteria.andLike("nickName","%"+user.getNickName()+"%");
				//criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(StringUtils.isNotBlank(user.getName())){
				criteria.andLike("name","%"+user.getName()+"%");
				//criteria.andNameLike("%"+user.getName()+"%");
			}
			if(StringUtils.isNotBlank(user.getStatus())){
				criteria.andLike("status","%"+user.getStatus()+"%");
				//criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(user.getHeadPic())){
				criteria.andLike("headPic","%"+user.getHeadPic()+"%");
				//criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(StringUtils.isNotBlank(user.getQq())){
				criteria.andLike("qq","%"+user.getQq()+"%");
				//criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(StringUtils.isNotBlank(user.getIsMobileCheck())){
				criteria.andLike("isMobileCheck","%"+user.getIsMobileCheck()+"%");
				//criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(StringUtils.isNotBlank(user.getIsEmailCheck())){
				criteria.andLike("isEmailCheck","%"+user.getIsEmailCheck()+"%");
				//criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(StringUtils.isNotBlank(user.getSex())){
				criteria.andLike("sex","%"+user.getSex()+"%");
				//criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
        List<TbUser> all = userMapper.selectByExample(example);
        PageInfo<TbUser> info = new PageInfo<TbUser>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Autowired
	private RedisTemplate redisTemplate;
	@Value("${sign_name}")
 	private String sign_name;
	@Value("${template_code}")
	private String template_code;
	@Autowired
	private DefaultMQProducer producer;

	@Override
	public void createCode(String phone) {
		//1.生成6位数字
		String code =  (long) ((Math.random() * 9 + 1) * 100000)+"";
		//2.存储到redis
        redisTemplate.boundValueOps("ZHUCE_"+phone).set(code);
		redisTemplate.boundValueOps("ZHUCE_"+phone).expire(24L, TimeUnit.HOURS); //设置过期时间

		//3.组装消息对象，发送信息
		Map<String,String> map = new HashMap<>();
		map.put("mobile",phone);
		map.put("sign_name",sign_name);
		map.put("template_code",template_code);
		map.put("param","{\"code\":\""+code+"\"}");
		//4.发送信息

		try {
			Message message = new Message(
					"SMS_TOPIC",
					"SEND_MESSAGE_TAG",
					"createSmsCode",
					JSON.toJSONString(map).getBytes());
			producer.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkCode(String phone, String smscode) {
		if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(smscode)) {
			return false;
		}
		String code= (String) redisTemplate.boundValueOps("ZHUCE_" + phone).get();

		if (!smscode.equals(code)) {
			return false;
		}


  		return true;
	}


	@Override
	public void add(TbUser record) {

		//密码加密
		String password = record.getPassword();
		String passwordencod = DigestUtils.md5DigestAsHex(password.getBytes());
		record.setPassword(passwordencod);
		//补充必要的字段属性值
		record.setCreated(new Date());
		record.setUpdated(record.getCreated());

		//添加数据到数据库中
		userMapper.insert(record);
	}
}
