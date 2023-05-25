//package com.miya.system.module.search;
//
//import com.miya.common.annotation.Acl;
//import com.miya.common.model.dto.base.R;
//import com.miya.system.module.user.model.SysUser;
//import com.miya.system.module.user.model.SysUserPrincipal;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import jakarta.validation.constraints.NotBlank;
//import java.io.IOException;
//
//@Slf4j
//@Tag(name = "全文检索(todo)")
//@Acl(userType = SysUserPrincipal.class)
//@Validated
//@ResponseBody
//@RequestMapping(value = "search")
//@RequiredArgsConstructor
//public class SysSearchApi {
//
//    private final SysSearchService searchService;
//
//    @PostMapping("init")
//    @Operation(summary = "初始化")
//    public R<?> init() throws InterruptedException {
//        searchService.init();
//        return R.success();
//    }
//
//    @GetMapping
//    @Operation(summary = "搜索")
//    public R<?> search(@NotBlank String q) throws InterruptedException {
//        searchService.query(q);
//        return R.success();
//    }
//
//    @GetMapping("native")
//    @Operation(summary = "搜索")
//    public R<?> searchByNative(@NotBlank String q) throws IOException {
//        // searchService.queryByNative(q);
//        searchService.queryByNative(q);
//        return R.success();
//    }
//
//
//}
