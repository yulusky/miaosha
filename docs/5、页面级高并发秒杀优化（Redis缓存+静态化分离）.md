[TOC]


## 页面缓存（URL缓存）

1.取缓存
2.手动渲染模板
3.结果输出 

针对商品列表和商品详情做缓存

商品列表QPS对比：
	· 没缓存之前：1200
	· 缓存之后：2400

不做页面缓存

```java
@RequestMapping(value="/to_list")
    public String list(Model model,MiaoshaUser user) {
    	model.addAttribute("user", user);
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	model.addAttribute("goodsList", goodsList);
    	 return "goods_list";
    }
```

做了缓存

```java
@RequestMapping(value="/to_list", produces="text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user) {
    	model.addAttribute("user", user);
    	//取缓存
    	String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
    	if(!StringUtils.isEmpty(html)) {
    		return html;
    	}
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	model.addAttribute("goodsList", goodsList);
//    	 return "goods_list";
    	SpringWebContext ctx = new SpringWebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
    	//手动渲染
    	html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(GoodsKey.getGoodsList, "", html);
    	}
    	return html;
    }
```

## 对象缓存

1.key为id，value为MiaoshaUser

2.将订单也可以缓存

```java
public MiaoshaUser getById(long id) {
		//取缓存
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null) {
			return user;
		}
		//取数据库
		user = miaoshaUserDao.getById(id);
		if(user != null) {
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}
```

## 页面静态化，前后端分离

对商品详情，订单等页面做静态化

```java
	@RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	long startAt = goods.getStartDate().getTime();
    	long endAt = goods.getEndDate().getTime();
    	long now = System.currentTimeMillis();
    	int miaoshaStatus = 0;
    	int remainSeconds = 0;
    	if(now < startAt ) {//秒杀还没开始，倒计时
    		miaoshaStatus = 0;
    		remainSeconds = (int)((startAt - now )/1000);
    	}else  if(now > endAt){//秒杀已经结束
    		miaoshaStatus = 2;
    		remainSeconds = -1;
    	}else {//秒杀进行中
    		miaoshaStatus = 1;
    		remainSeconds = 0;
    	}
    	GoodsDetailVo vo = new GoodsDetailVo();
    	vo.setGoods(goods);
    	vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setMiaoshaStatus(miaoshaStatus);
    	return Result.success(vo);
    }
 
```

```html
<!DOCTYPE HTML>
<html >
<head>
    <title>商品详情</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <!-- jquery -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <!-- bootstrap -->
    <link rel="stylesheet" type="text/css" href="/bootstrap/css/bootstrap.min.css" />
    <script type="text/javascript" src="/bootstrap/js/bootstrap.min.js"></script>
    <!-- jquery-validator -->
    <script type="text/javascript" src="/jquery-validation/jquery.validate.min.js"></script>
    <script type="text/javascript" src="/jquery-validation/localization/messages_zh.min.js"></script>
    <!-- layer -->
    <script type="text/javascript" src="/layer/layer.js"></script>
    <!-- md5.js -->
    <script type="text/javascript" src="/js/md5.min.js"></script>
    <!-- common.js -->
    <script type="text/javascript" src="/js/common.js"></script>
</head>
<body>

<div class="panel panel-default">
  <div class="panel-heading">秒杀商品详情</div>
  <div class="panel-body">
  	<span id="userTip"> 您还没有登录，请登陆后再操作<br/></span>
  	<span>没有收货地址的提示。。。</span>
  </div>
  <table class="table" id="goodslist">
  	<tr>  
        <td>商品名称</td>  
        <td colspan="3" id="goodsName"></td> 
     </tr>  
     <tr>  
        <td>商品图片</td>  
        <td colspan="3"><img  id="goodsImg" width="200" height="200" /></td>  
     </tr>
     <tr>  
        <td>秒杀开始时间</td>  
        <td id="startTime"></td>
        <td >	
        	<input type="hidden" id="remainSeconds" />
        	<span id="miaoshaTip"></span>
        </td>
        <td>
        <!--  
        	<form id="miaoshaForm" method="post" action="/miaosha/do_miaosha">
        		<button class="btn btn-primary btn-block" type="submit" id="buyButton">立即秒杀</button>
        		<input type="hidden" name="goodsId"  id="goodsId" />
        	</form>-->
        	<button class="btn btn-primary btn-block" type="button" id="buyButton"onclick="doMiaosha()">立即秒杀</button>
        	<input type="hidden" name="goodsId"  id="goodsId" />
        </td>
     </tr>
     <tr>  
        <td>商品原价</td>  
        <td colspan="3" id="goodsPrice"></td>  
     </tr>
      <tr>  
        <td>秒杀价</td>  
        <td colspan="3"  id="miaoshaPrice"></td>  
     </tr>
     <tr>  
        <td>库存数量</td>  
        <td colspan="3"  id="stockCount"></td>  
     </tr>
  </table>
</div>
</body>
<script>

function doMiaosha(){
	$.ajax({
		url:"/miaosha/do_miaosha",
		type:"POST",
		data:{
			goodsId:$("#goodsId").val(),
		},
		success:function(data){
			if(data.code == 0){
				window.location.href="/order_detail.htm?orderId="+data.data.id;
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(){
			layer.msg("客户端请求有误");
		}
	});
	
}

function render(detail){
	var miaoshaStatus = detail.miaoshaStatus;
	var  remainSeconds = detail.remainSeconds;
	var goods = detail.goods;
	var user = detail.user;
	if(user){
		$("#userTip").hide();
	}
	$("#goodsName").text(goods.goodsName);
	$("#goodsImg").attr("src", goods.goodsImg);
	$("#startTime").text(new Date(goods.startDate).format("yyyy-MM-dd hh:mm:ss"));
	$("#remainSeconds").val(remainSeconds);
	$("#goodsId").val(goods.id);
	$("#goodsPrice").text(goods.goodsPrice);
	$("#miaoshaPrice").text(goods.miaoshaPrice);
	$("#stockCount").text(goods.stockCount);
	countDown();
}

$(function(){
	//countDown();
	getDetail();
});

function getDetail(){
	var goodsId = g_getQueryString("goodsId");
	$.ajax({
		url:"/goods/detail/"+goodsId,
		type:"GET",
		success:function(data){
			if(data.code == 0){
				render(data.data);
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(){
			layer.msg("客户端请求有误");
		}
	});
}

function countDown(){
	var remainSeconds = $("#remainSeconds").val();
	var timeout;
	if(remainSeconds > 0){//秒杀还没开始，倒计时
		$("#buyButton").attr("disabled", true);
	   $("#miaoshaTip").html("秒杀倒计时："+remainSeconds+"秒");
		timeout = setTimeout(function(){
			$("#countDown").text(remainSeconds - 1);
			$("#remainSeconds").val(remainSeconds - 1);
			countDown();
		},1000);
	}else if(remainSeconds == 0){//秒杀进行中
		$("#buyButton").attr("disabled", false);
		if(timeout){
			clearTimeout(timeout);
		}
		$("#miaoshaTip").html("秒杀进行中");
	}else{//秒杀已经结束
		$("#buyButton").attr("disabled", true);
		$("#miaoshaTip").html("秒杀已经结束");
	}
}

</script>
</html>

```

## 静态资源优化

1.JS/CSS压缩，减少流量
2.多个JS/CSS组合，减少连接数
3.CDN就近访问

## CDN优化