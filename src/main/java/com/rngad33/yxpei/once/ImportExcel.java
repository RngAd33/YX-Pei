package com.rngad33.yxpei.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * 导入Excel
 */
@Slf4j
public class ImportExcel {

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, TableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读取
     *
     * @param fileName
     */
    public static void synchronousRead(String fileName) {
        // 这里需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<TableUserInfo> totalDataList = EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        for (TableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
    }

    /**
     * 测试方法
     *
     * @param args
     */
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir")
                + File.separator + "src/main/java/com/rngad33/yxpei/model/temp/text.xlsx";
        // readByListener(fileName);
        synchronousRead(fileName);
    }
}