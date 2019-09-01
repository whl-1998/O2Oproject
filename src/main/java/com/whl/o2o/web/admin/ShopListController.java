package com.whl.o2o.web.admin;

import com.whl.o2o.dto.ShopExecution;
import com.whl.o2o.entity.Area;
import com.whl.o2o.entity.Shop;
import com.whl.o2o.entity.ShopCategory;
import com.whl.o2o.service.AreaService;
import com.whl.o2o.service.ShopCategoryService;
import com.whl.o2o.service.ShopService;
import com.whl.o2o.util.HttpServletRequestUtil;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("frontend")
public class ShopListController {
    @Autowired
    private AreaService areaService;
    @Autowired
    private ShopCategoryService shopCategoryService;
    @Autowired
    private ShopService shopService;

    @RequestMapping(value = "listshopspageinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String, Object> listShopsPageInfo(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        long parentId = HttpServletRequestUtil.getLong(request,"parentId");
        List<ShopCategory> shopCategoryList = null;
        //如果parentId是存在的,则取出该根分类下的子分类列表
        if(parentId != -1){
            try {
                ShopCategory shopCategoryCondition = new ShopCategory();
                ShopCategory parent = new ShopCategory();
                parent.setShopCategoryId(parentId);
                shopCategoryCondition.setParent(parent);
                shopCategoryList = shopCategoryService.getShopCategoryList(shopCategoryCondition);
            }catch (Exception e){
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
            }
        }else {
            try {
                //如果parentId不存在,则取出所有根分类(首页的分类)
                shopCategoryList = shopCategoryService.getShopCategoryList(null);
            }catch (Exception e){
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
            }
        }
        modelMap.put("shopCategoryList",shopCategoryList);
        List<Area> areaList = null;
        try {
            //获取区域列表信息
            areaList = areaService.getAreaList();
            modelMap.put("areaList",areaList);
            modelMap.put("success",true);
            return modelMap;
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }

    @RequestMapping(value = "listshops",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> listShops(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        //页码
        int pageIndex = HttpServletRequestUtil.getInt(request,"pageIndex");
        //每一页显示的数据条数
        int pageSize = HttpServletRequestUtil.getInt(request,"pageSize");
        //非空判断
        if((pageIndex>-1)&&(pageSize>-1)){
            long parentId = HttpServletRequestUtil.getLong(request,"parentId");
            long shopCategoryId = HttpServletRequestUtil.getLong(request,"shopCategory");
            int areaId = HttpServletRequestUtil.getInt(request,"areaId");
            String shopName = HttpServletRequestUtil.getString(request,"shopName");
            //获取组合查询条件
            Shop shopCondition = compactShopCondition4Search(parentId,shopCategoryId,areaId,shopName);
            ShopExecution shopExecution = shopService.getShopList(shopCondition,pageIndex,pageSize);
            modelMap.put("shopList",shopExecution.getShopList());
            modelMap.put("count",shopExecution.getCount());
            modelMap.put("success",true);
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","EMPTY pageSize or pageIndex");
        }
        return modelMap;
    }

    private Shop compactShopCondition4Search(long parentId, long shopCategoryId, int areaId, String shopName) {
        Shop shopCondition = new Shop();
        if(parentId != 1L){
            ShopCategory childCategory = new ShopCategory();
            ShopCategory parentCategory = new ShopCategory();
            parentCategory.setShopCategoryId(parentId);
            childCategory.setParent(parentCategory);
            shopCondition.setShopCategory(childCategory);
        }
        if(shopCategoryId != -1L){
            //查询某个子分类下的所以店铺列表
            ShopCategory shopCategory = new ShopCategory();
            shopCategory.setShopCategoryId(shopCategoryId);
            shopCondition.setShopCategory(shopCategory);
        }
        if(areaId != -1L){
            Area area = new Area();
            area.setAreaId(areaId);
            shopCondition.setArea(area);
        }
        if(shopName != null){
            shopCondition.setShopName(shopName);
        }
        //前端展示的都是审核通过的店铺
        shopCondition.setEnableStatus(1);
        return shopCondition;

    }
}
