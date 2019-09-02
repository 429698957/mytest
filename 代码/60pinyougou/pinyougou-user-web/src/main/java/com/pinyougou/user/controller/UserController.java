package com.pinyougou.user.controller;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;

import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.Error;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	
	@RequestMapping("/findPage")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return userService.findPage(pageNo, pageSize);
    }
	
	/**
	 * 增加
	 * @param user
	 * @param smscode 表示我们页面传递的验证码
	 * @return
	 */
	@RequestMapping("/add/{smscode}")
	public Result add(@RequestBody TbUser user, BindingResult bindingResult, @PathVariable(name="smscode") String smscode){
		try {
			if(bindingResult.hasErrors()){
				Result result = new Result(false,"失败");
				List<FieldError> fieldErrors = bindingResult.getFieldErrors();
				for (FieldError fieldError : fieldErrors) {
					result.getErrorsList().add(new Error(fieldError.getField(),fieldError.getDefaultMessage()));
				}
				return result;
			}
			//1.判断验证码是否正确
			if (!userService.checkCode(user.getPhone(),smscode)) {
				return new Result(false,"验证码错误");

			}
			//2.正确才放行
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne/{id}")
	public TbUser findOne(@PathVariable(value = "id") Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbUser user) {
        return userService.findPage(pageNo, pageSize, user);
    }

    @RequestMapping("/sendCode")
	public Result sendCode(String phone){
		try {
			userService.createCode(phone);
			return new Result(true,"请查看手机");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"失败");

		}
	}
	
}
