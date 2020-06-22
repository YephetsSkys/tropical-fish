package com.mcoding.modular.base.user.controller;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mcoding.base.orm.SmartWrapper;
import com.mcoding.base.rest.ResponseResult;
import com.mcoding.common.util.excel.ExcelUtils;
import com.mcoding.common.util.excel.TitleAndModelKey;
import com.mcoding.modular.base.user.entity.BaseUser;
import com.mcoding.modular.base.user.service.BaseUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.write.WritableWorkbook;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基础用户
 * </p>
 *
 * @author wzt
 * @since 2020-06-21
 */
@Api(tags = "基础-用户服务")
@RestController
public class BaseUserController {

    @Resource
    private BaseUserService baseUserService;

    @ApiOperation("创建")
    @PostMapping("/service/user/create")
    public ResponseResult<String> create(@Valid @RequestBody BaseUser baseUser) {
        baseUserService.save(baseUser);
        return ResponseResult.success();
    }

    @ApiOperation(value = "删除")
    @PostMapping("/service/user/delete")
    public ResponseResult<String> delete(@RequestParam Integer id) {
        baseUserService.removeById(id);
        return ResponseResult.success();
    }

    @ApiOperation(value = "修改")
    @PostMapping("/service/user/modify")
    public ResponseResult<String> modify(@Valid @RequestBody BaseUser baseUser) {
        baseUserService.updateById(baseUser);
        return ResponseResult.success();
    }

    @ApiOperation(value = "查询活动详情")
    @GetMapping("/service/user/detail")
    public ResponseResult<BaseUser> detail(@RequestParam Integer id) {
        return ResponseResult.success(baseUserService.getById(id));
    }

    @ApiOperation(value = "分页查询")
    @PostMapping("/service/user/queryByPage")
    public ResponseResult<IPage<BaseUser>> queryByPage(@RequestBody JSONObject queryObject) {

        SmartWrapper<BaseUser> smartWrapper = new SmartWrapper<>();
        smartWrapper.parse(queryObject, BaseUser.class);

        QueryWrapper<BaseUser> queryWrapper = smartWrapper.getQueryWrapper();
        IPage<BaseUser> page = smartWrapper.generatePage();
        baseUserService.page(page, queryWrapper);
        return ResponseResult.success(page);
    }


    @ApiOperation("导出")
    @GetMapping(value = "/service/user/exportByExcel")
    @ResponseBody
    public ResponseResult<String> exportByExcel(
            @RequestParam(required = false) Map<String, Object> queryParam,
            HttpServletResponse httpServletResponse) throws Exception {

        String fileName = "用户明细" + DateUtil.format(new Date(), "yyyyMMddHHmmss");

        httpServletResponse.reset();
        httpServletResponse.setContentType("application/vnd.ms-excel;charset=utf-8");
        httpServletResponse.setHeader("Content-Disposition", String.format("attachment;filename=%s.xls",
                new String(fileName.getBytes("UTF-8"), "ISO8859-1")));
        httpServletResponse.addHeader("Cache-Control", "no-cache");

        OutputStream outputStream = httpServletResponse.getOutputStream();
        List<TitleAndModelKey> titleAndModelKeyList = ExcelUtils.createTitleAndModelKeyList(BaseUser.class);

        JSONObject queryObject = new JSONObject(queryParam);

        SmartWrapper<BaseUser> smartWrapper = new SmartWrapper<>();
        smartWrapper.parse(queryObject, BaseUser.class);

        QueryWrapper<BaseUser> queryWrapper = smartWrapper.getQueryWrapper();
        queryWrapper.lambda().orderByDesc(BaseUser::getCreateTime);

        List<BaseUser> activityOrderList = this.baseUserService.list(queryWrapper);

        WritableWorkbook writableWorkbook = ExcelUtils.exportDataToExcel(outputStream, titleAndModelKeyList, activityOrderList, "用户", null, 0);
        writableWorkbook.write();
        writableWorkbook.close();

        return null;
    }


}
