package whut.service.impl;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import whut.dao.ProInfoDao;
import whut.dao.ProPicInfoDao;
import whut.pojo.ProductCategory;
import whut.pojo.ProductInfo;
import whut.pojo.ProductPicInfo;
import whut.service.ProDiscountService;
import whut.service.ProInfoService;
import whut.utils.JedisUtil;
import whut.utils.JsonUtils;
import whut.utils.ResponseData;
import whut.utils.SolrJUtil;
import whut.utils.SysContent;



@Service
public class ProInfoServiceImpl implements ProInfoService{

	@Autowired
	private ProInfoDao proInfoDao;
	
	@Autowired
	private ProDiscountService proDiscountService;
	
	@Autowired
	private ProPicInfoDao proPicInfoDao;
	 
	//查找返回的查询项
	String[] queryItem = new String[] {"productId", "productName","discountRate","pscore","mainImage","minPrice","maxPrice","description","sales"}; 
	
	@Override
	public ResponseData getList(Integer pageindex, Integer pagesize) {
		if(pageindex == null)
			pageindex = 0;
		if(pagesize == null)
			pagesize = 20;
		Map<String,Object> map = new HashMap<>();
		map.put("pageindex", pageindex);
		map.put("pagesize", pagesize);
		List<ProductInfo> list = proInfoDao.getList(map);
		//获取商品表的总数量		
		if(list != null) {
			Integer num = proInfoDao.getListNum();
			return new ResponseData(200,"success",list,num);
		}else {
			return new ResponseData(400,"no data",null);
		}
	}
	
	
	@Override
	public ResponseData getListSearch(Integer pageindex, Integer pagesize,String field,Byte judge) {
		if(pageindex == null) {
			pageindex = 1;
		}
		if(pagesize == null){
			pagesize = 20;
		}
		if(judge == null) {			
			judge = 0;		//不传judge的状态默认无序（不按照递增递减排序）
		}	
		if(field == "sales" || field =="pscore" || field == "inputTime") {
			judge = -1;		//如果按销量、评分、录入时间排序则是默认从高到低递减排序（降序）
		}	
		return new ResponseData(200,"success",SolrJUtil.searchNew(pageindex,pagesize,"*:*",queryItem,field,judge,null));
	}

	@Override
	public ResponseData getDetail(String id) {
		ProductInfo productInfo = proInfoDao.getDetail(id);
		if(productInfo != null) {
			//用户点击一个商品详情则记录到Redis浏览信息中（view:商品id）是键，（次数）是值
			Jedis jedis = null;
			try {
			    jedis = JedisUtil.getJedis();
			    if(jedis.hget("productView",id) == null) {		//获取商品id对应的次数,如果为空说明未存过
			        jedis.hset("productView",id, "1");		//第一次存1
			    }else {
			        jedis.hincrBy("productView", id, 1);		//对查询到的商品id对应的值，即次数，自增1
			    }
			} catch (Exception e) {
				//e.printStackTrace();
			}
			//System.out.println(jedis.get("view:"+id));	//测试输出，根据键（view：商品id）查值（次数）
	    	finally{
	    		if(jedis != null)
	    			JedisUtil.closeJedis(jedis);
	    	}
	    	BigDecimal rate = new BigDecimal(100).subtract(proDiscountService.getDiscountRateById(id));	//算出普通会员的折扣率
			/*System.out.println(rate);
			System.out.println(productInfo.getMinPrice());*/
	    	productInfo.setMinPriceVip(productInfo.getMinPrice().multiply(rate).divide(new BigDecimal(100)));
			productInfo.setMaxPriceVip(productInfo.getMaxPrice().multiply(rate).divide(new BigDecimal(100)));
			return new ResponseData(200,"success",productInfo);
		}else {
			return new ResponseData(400,"no data",null);
		}
	}

	

	@Override
	public ResponseData search(String name,Integer pageindex, Integer pagesize,String field,Byte judge) {
		//pageindex从1开始
		if(pageindex == null) {
			pageindex = 1;
		}
		if(pagesize == null){
			pagesize = 20;
		}
		if(judge == null) {			
			judge = 0;		//不传judge的状态默认无序（不按照递增递减排序）
		}	
		if(field == "sales" || field =="pscore" || field == "inputTime") {
			judge = -1;		//如果按销量、评分、录入时间排序则是默认从高到低递减排序（降序）
		}	
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getJedis();
			String key = "searchKey:"+SysContent.getUserId()+"";
			jedis.lrem(key, 0, name);
			if(jedis.llen(key) < 20) {	//获取set集合长度,从0开始保存20条
				jedis.lpush(key,name);		//如果用户登录了就把用户id和搜索内容存入Redis				
			}else {
				jedis.rpop(key);		//删除最先加入的一个值
				jedis.lpush(key,name);
			}
			
			//SolrJUtil.search(pageindex,pagesize,"productName:"+name,new String[] {"productId", "productName","discountRate","price","mainImage"},null,null,null);	//测试
		} catch (Exception e) {
			//如果用户未登录，则获取不到用户ID，SysContent.getUserId()方法会抛出异常，这里不做处理
		} finally {
			if(jedis != null)
				JedisUtil.closeJedis(jedis);
		}
		return new ResponseData(200,"success",SolrJUtil.searchNew(pageindex,pagesize,"productName:"+name,queryItem,field,judge,null));
	}

	@Override
	public ResponseData getListByCategory(String id,Integer pageindex, Integer pagesize) {
		if(pageindex == null)
			pageindex = 0;
		if(pagesize == null)
			pagesize = 20;
		Map<String,Object> map = new HashMap<>();
		map.put("oneCategoryId", id);
		map.put("twoCategoryId", id);
		map.put("threeCategoryId", id);
		map.put("pageindex", pageindex);
		map.put("pagesize", pagesize);
		List<ProductInfo> list = proInfoDao.getListByCategory(map);
		if(list.isEmpty())
			return new ResponseData(400,"No data",null);
		Integer num = proInfoDao.getListByCategoryNum(id);
		return new ResponseData(200,"success",list,num);
	}
	
	@Override
	public ResponseData getListByCategorySearch(String id, Integer pageindex, Integer pagesize,String field,Byte judge) {
		if(pageindex == null) {
			pageindex = 1;
		}
		if(pagesize == null){
			pagesize = 20;
		}
		if(judge == null) {			
			judge = 0;		//不传judge的状态默认无序（不按照递增递减排序）
		}	
		if(field == "sales" || field =="pscore" || field == "inputTime") {
			judge = -1;		//如果按销量、评分、录入时间排序则是默认从高到低递减排序（降序）
		}
		String searchCondition = "oneCategoryId:"+id+" || twoCategoryId:"+id;
		return new ResponseData(200,"success",SolrJUtil.searchNew(pageindex,pagesize,searchCondition,queryItem,field,judge,null));
	}

	@Override
	public ResponseData getDetailByCode(String id) {
		ProductInfo productInfo = proInfoDao.getDetailByCode(id);
		if(productInfo != null) {
			return new ResponseData(200,"success",productInfo);
		}else {
			return new ResponseData(400,"no data",null);
		}
	}


	@Override
	public ResponseData getPicList(String id) {
		List<ProductPicInfo> proPiclist = proPicInfoDao.getPicList(id);
		if(proPiclist != null) {
			return new ResponseData(200,"success",proPiclist);
		}else {
			return new ResponseData(400,"no data",null);
		}
	}


	@Override
	public ResponseData getRecommendPro() {
		
		// 查询缓存
		try {
			Jedis jedis = JedisUtil.getJedis();
			String jsonstr = jedis.get("recommendPro");
			JedisUtil.closeJedis(jedis);
			// 如果存在，说明有缓存
			if (jsonstr != null) {
				List<ProductInfo> list = JsonUtils.jsonToList(jsonstr, ProductInfo.class);
				return new ResponseData(200, "success", list);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		List<ProductInfo> list = proInfoDao.getRecommendPro();
		if(list != null) {
			try {
				Jedis jedis = JedisUtil.getJedis();
				jedis.set("recommendPro", JsonUtils.objectToJson(list));
				JedisUtil.closeJedis(jedis);
			}catch (Exception e) {
				e.printStackTrace();
			}
			return new ResponseData(200,"success",list);
		}else {
			return new ResponseData(400,"no data",null);
		}

	}
	
}
