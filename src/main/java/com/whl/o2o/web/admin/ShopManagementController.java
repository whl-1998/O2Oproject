package com.whl.o2o.web.admin;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.whl.o2o.dto.ShopExecution;
import com.whl.o2o.entity.Area;
import com.whl.o2o.entity.Shop;
import com.whl.o2o.entity.ShopCategory;
import com.whl.o2o.entity.UserInfo;
import com.whl.o2o.enums.ShopStateEnum;
import com.whl.o2o.service.AreaService;
import com.whl.o2o.service.ShopCategoryService;
import com.whl.o2o.service.ShopService;
import com.whl.o2o.util.CodeUtil;
import com.whl.o2o.util.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author whl
 * @version V1.0
 * @Title:
 * @Description:
 */
@Controller
@RequestMapping("/shopadmin")
public class ShopManagementController {
    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopCategoryService shopCategoryService;

    @Autowired
    private AreaService areaService;

    /**
     * 通过shopId获取shop以及所有的areaList
     * @param request
     * @return
     */
    @RequestMapping(value = "/getshopbyid",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopById(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        Long shopId = HttpServletRequestUtil.getLong(request,"shopId");
        if(shopId > -1) {
            try {
                Shop shop = shopService.getByShopId(shopId);
                List<Area> areaList = areaService.getAreaList();
                modelMap.put("shop", shop);
                modelMap.put("areaList", areaList);//获取的所有area用于修改时的下拉表单展示
                modelMap.put("success", true);
            }catch (Exception e){
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
            }
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","empty shopId");
        }
        return modelMap;
    }

    /**
     * 获取shop的区域以及类别信息返回给前端
     * @return
     */
    @RequestMapping(value = "/getshopinitinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopInitInfo(){
        Map<String,Object> modelMap = new HashMap<>();
        List<ShopCategory> shopCategoryList = new ArrayList<>();
        List<Area> areaList = new ArrayList<>();
        try {
            shopCategoryList = shopCategoryService.getShopCategoryList(new ShopCategory());//获取所有店铺类别
            areaList = areaService.getAreaList();
            modelMap.put("shopCategoryList",shopCategoryList);
            modelMap.put("areaList",areaList);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }

    /**
     * 店铺注册
     * @param request
     * @return
     */
    @RequestMapping(value = "/registershop",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> registerShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        //通过从前端获取的request校验验证码是否一致
        if(!CodeUtil.checkVerifyCode(request)){
                modelMap.put("success",false);
                modelMap.put("errMsg","输入验证码有误");
                return modelMap;
        }
        //1.接受并转化相应的参数
        String shopStr = HttpServletRequestUtil.getString(request,"shopStr");//获取key为shopStr的value并转换为String
        ObjectMapper mapper = new ObjectMapper();
        Shop shop = null;
        try {
            //将前端获取的信息转化为Shop实体类
            shop = mapper.readValue(shopStr,Shop.class);
        } catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;//若转换出现异常则返回一个附带错误信息的map对象
        }
        //采用cmp接收图片信息
        CommonsMultipartFile shopImg = null;
        //解析
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if(commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            shopImg = (CommonsMultipartFile)multipartHttpServletRequest.getFile("shopImg");
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","上传图片不能为空");
            return modelMap;
        }
        //2.注册店铺
        if(shop != null && shopImg !=null){
            //可以通过session获取user信息
            UserInfo owner = (UserInfo) request.getSession().getAttribute("user");
            shop.setUserInfo(owner);
            ShopExecution shopExecution = null;//将shop实体类对象和img交给service处理并返回一个执行结果
            try {
                shopExecution = shopService.addShop(shop,shopImg.getInputStream(),shopImg.getOriginalFilename());
                if(shopExecution.getState()== ShopStateEnum.CHECK.getState()) {//创建成功
                    modelMap.put("success", true);
                    //一个用户可以创建多个店铺,获取当前用户可操作所有的shop
                    List<Shop> shopList = (List<Shop>) request.getSession().getAttribute("shopList");
                    if (shopList == null || shopList.size() == 0) {
                        shopList = new ArrayList<>();
                    }
                    shopList.add(shopExecution.getShop());
                    request.getSession().setAttribute("shopList", shopList);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",shopExecution.getStateInfo());//如果出错返回对应的执行状态
                }
            } catch (IOException e) {
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
            }
            return modelMap;
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入店铺信息");
            return modelMap;
        }
    }

    /**
     * 店铺修改
     * @param request
     * @return
     */
    @RequestMapping(value = "/modifyshop",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> modifyshop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        if(!CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","输入验证码有误");
            return modelMap;
        }
        //1.接受并转化相应的参数
        String shopStr = HttpServletRequestUtil.getString(request,"shopStr");//获取key为shopStr的value并转换为String
        ObjectMapper mapper = new ObjectMapper();
        Shop shop = null;
        try {
            //将前端获取的信息转化为Shop实体类
            shop = mapper.readValue(shopStr,Shop.class);
        } catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;//若转换出现异常则返回一个附带错误信息的map对象
        }
        //采用cmp接收图片信息
        CommonsMultipartFile shopImg = null;
        //解析
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if(commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            shopImg = (CommonsMultipartFile)multipartHttpServletRequest.getFile("shopImg");
        }
        //2.修改店铺
        if(shop != null && shop.getShopId()!=null){
            //可以通过session获取user信息
            UserInfo owner = new UserInfo();
            owner.setUserId(1L);
            shop.setUserInfo(owner);
            ShopExecution shopExecution = null;//将shop实体类对象和img交给service处理并返回一个执行结果
            try {
                if(shopImg == null){
                    shopExecution = shopService.modifyShop(shop,null,null);
                }else {
                    shopExecution = shopService.modifyShop(shop,shopImg.getInputStream(), shopImg.getOriginalFilename());
                }
                if(shopExecution.getState()== ShopStateEnum.SUCCESS.getState()){//创建成功
                    modelMap.put("success",true);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",shopExecution.getStateInfo());//如果出错返回对应的执行状态
                }
            } catch (IOException e) {
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
            }
            return modelMap;
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入店铺id");
            return modelMap;
        }
    }


    /**
     * 根据当前登陆的用户信息，返回该用户创建的shoplist
     * @param request
     * @return
     */
    @RequestMapping(value = "/getshoplist",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopList(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(1L);
        if(userInfo.getUserId() == null){
            modelMap.put("success",false);
            modelMap.put("errMsg","userId为空");
        }
        request.getSession().setAttribute("user",userInfo);
        userInfo = (UserInfo) request.getSession().getAttribute("user");
        List<Shop> shopList = new ArrayList<>();
        try {
            Shop shopCondition = new Shop();
            shopCondition.setUserInfo(userInfo);
            ShopExecution shopExecution = shopService.getShopList(shopCondition,0,100);
            modelMap.put("shopList",shopExecution.getShopList());
            modelMap.put("user",userInfo);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg","userId为空");
        }
        return modelMap;
    }

    /**
     * 用于管理session相关的操作
     * @param request
     * @return
     */
    @RequestMapping(value = "/getshopmanagementinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopManagementInfo(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        Long shopId = HttpServletRequestUtil.getLong(request,"shopId");
        if(shopId <= 0){
            Object currentShopObj = request.getSession().getAttribute("currentShop");
            if(currentShopObj == null){
                modelMap.put("redirect",true);
                modelMap.put("url","/o2o/shop/shoplist");
            }else {
                Shop currentShop = (Shop)currentShopObj;
                modelMap.put("redirect",false);
                modelMap.put("shopId",currentShop.getShopId());
            }
        }else {
            Shop currentShop = new Shop();
            currentShop.setShopId(shopId);
            request.getSession().setAttribute("currentShop",currentShop);
            modelMap.put("redirect",false);
        }
        return modelMap;
    }
}
