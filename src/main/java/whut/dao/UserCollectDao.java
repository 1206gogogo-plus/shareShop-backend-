package whut.dao;

import java.util.List;
import java.util.Map;

import whut.pojo.UserCollect;

public interface UserCollectDao {

	List<UserCollect> getListByUser(int id);

	Object getAmountById(int id);

	
	//通过productId、userId获取唯一的一条收藏信息
	UserCollect getCollect(Map<String, Integer> map);

	//新增收藏
	void add(UserCollect userCollect);
	
	//通过用户id和商品id取消收藏
	void cancel(Map<String, Integer> map);

	//通过收藏id删除收藏
	void delete(int collectId);

	//通过collectId获取收藏
	UserCollect getCollectByCollectId(int collectId);

	//获取某用户收藏商品数量
	Integer getCollectAmountByUser(int userId);


}