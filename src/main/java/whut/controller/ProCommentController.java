package whut.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import whut.pojo.ProductComment;
import whut.service.ProCommentService;
import whut.utils.ResponseData;

@Controller
@RequestMapping(value = "/pro/comment")
public class ProCommentController {
	
	@Autowired
	private ProCommentService proCommentService;
	
	//根据商品id获取评论列表
	@RequestMapping(value = "/getListByProduct", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByProduct(String id, Integer pageindex, Integer pagesize) {
		return proCommentService.getListByProduct(id,pageindex, pagesize);
	}
	
	//根据用户id查询用户评论列表
	@RequestMapping(value = "/getListByUser", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByUser(String id, Integer pageindex, Integer pagesize) {
		return proCommentService.getListByUser(id,pageindex, pagesize);
	}
	
	//新增评论第一次
	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody ResponseData add(@RequestBody ProductComment productComment) {
		return proCommentService.add(productComment);
		
	}
	
	//根据评论id追加评论第二次
	@RequestMapping(value = "/addAgain", method = RequestMethod.GET)
	public @ResponseBody ResponseData addAgain(String id, String content) {
		return proCommentService.addAgain(id, content);
	}
	
}
