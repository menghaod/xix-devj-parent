package com.xix.sdk.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class XPage<E> {
    private Long pageNum;
    private Long pageSize;
    private Long totalCount;
    private Long totalPage;
    private List<E> items;


    public static <E> XPage<E> iPage2JPage(IPage<E> iPage) {
        XPage<E> xPage = new XPage<>();
        xPage.setPageNum(iPage.getCurrent());
        xPage.setPageSize(iPage.getSize());
        xPage.setTotalCount(iPage.getTotal());
        xPage.setTotalPage(iPage.getPages());
        xPage.setItems(iPage.getRecords());
        return xPage;
    }

    public static <E, R> XPage<R> iPage2JPage(IPage<E> iPage, List<R> data) {
        XPage<R> xPage = new XPage<>();
        xPage.setPageNum(iPage.getCurrent());
        xPage.setPageSize(iPage.getSize());
        xPage.setTotalCount(iPage.getTotal());
        xPage.setTotalPage(iPage.getPages());
        xPage.setItems(data);
        return xPage;
    }
}
