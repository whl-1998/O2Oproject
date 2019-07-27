package com.whl.o2o.dao;

import com.whl.o2o.BaseTest;
import com.whl.o2o.entity.Area;
import com.whl.o2o.entity.Shop;
import com.whl.o2o.entity.ShopCategory;
import com.whl.o2o.entity.UserInfo;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Period;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author whl
 * @version V1.0
 * @Title:
 * @Description:
 */
public class ShopDaoTest extends BaseTest {
    @Autowired
    private ShopDao shopDao;

    @Test
    @Ignore
    public void testInsertShop(){
        Shop shop = new Shop();
        UserInfo userInfo = new UserInfo();
        Area area = new Area();
        ShopCategory shopCategory = new ShopCategory();
        userInfo.setUserId(1L);
        area.setAreaId(2);
        shopCategory.setShopCategoryId(1L);
        shop.setUserInfo(userInfo);
        shop.setArea(area);
        shop.setShopCategory(shopCategory);
        shop.setShopName("whl的吉他教室");
        shop.setShopDesc("吉他教室desc");
        shop.setShopAddr("合唱厅");
        shop.setPhone("15542381883");
        shop.setShopImg("test");
        shop.setCreateTime(new Date());
        shop.setEnableStatus(1);//可用
        shop.setPriority(1);
        shop.setAdvice("审核中");
        int effectedNum = shopDao.insertShop(shop);
        assertEquals(1,effectedNum);
    }

    @Test
    public void testUpdateShop(){
        Shop shop = new Shop();
        Area area = new Area();
        area.setAreaId(1);
        shop.setShopId(1L);
        shop.setArea(area);
        shop.setAdvice("测试testUpdateShop");
        shop.setUpdateTime(new Date());
        int effectedNum = shopDao.updateShop(shop);
        assertEquals(1,effectedNum);
    }
}
