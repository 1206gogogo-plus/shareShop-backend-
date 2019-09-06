package whut.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import whut.service.MemberOrderReturnService;
import whut.utils.ResponseData;

@Controller
@RequestMapping(value = "/member/orderReturn")
public class MemberOrderReturnController {
	@Autowired
	private MemberOrderReturnService memberOrderReturnService;
	
	/**
	 * 获取某用户的订单信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getListByUser", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByUser(Integer pageindex, Integer pagesize) {
		return memberOrderReturnService.getListByUser(pageindex, pagesize);
	}
	
	@RequestMapping(value = "/getListByStatus", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByStatus(int pageindex, int pagesize, int status) {
		return memberOrderReturnService.getListByStatus(pageindex, pagesize, status);
	}
	
	@RequestMapping(value = "/getListByOrderId", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByOrderId(int id) {
		return memberOrderReturnService.getListByOrderId(id);
	}
	
	@RequestMapping(value = "/getListByReturnType", method = RequestMethod.GET)
	public @ResponseBody ResponseData getListByReturnType(int pageindex, int pagesize, int type) {
		return memberOrderReturnService.getListByReturnType(pageindex, pagesize, type);
	}
	
	
	@RequestMapping(value = "/addReturn", method = RequestMethod.GET)
	public @ResponseBody ResponseData addReturn(@RequestBody String jsonString) {
		return memberOrderReturnService.addReturn(jsonString);
	}
}
