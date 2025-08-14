package com.rngad33.yxpei.once;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入用户到数据库
 */
public class ImportUsers {

    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir")
                + File.separator + "src/main/java/com/rngad33/yxpei/model/temp/demo.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<TableUserInfo> userInfoList =
                EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        System.out.println("总数：" + userInfoList.size());
        Map<String, List<TableUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StrUtil.isNotEmpty(userInfo.getUserName()))
                        .collect(Collectors.groupingBy(TableUserInfo::getUserName));
        for (Map.Entry<String, List<TableUserInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                System.out.println("username：" + stringListEntry.getKey());
                System.out.println("1");
            }
        }
        System.out.println("不重复昵称数：" + listMap.keySet().size());
    }

}