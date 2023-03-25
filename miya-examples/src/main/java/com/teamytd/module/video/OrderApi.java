// package com.teamytd.module.video;
//
// import com.miya.common.annotation.Acl;
// import com.miya.common.model.dto.base.R;
// import com.miya.system.module.search.SysSearchService;
// import com.miya.system.module.user.model.SysUser;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.bind.annotation.RestController;
//
// @RequestMapping(value = "order")
// @RestController
// @Slf4j
// @Api(tags = {"订单服务"})
// @Acl(userType = SysUserPrincipal.class)
// @Validated
// @RequiredArgsConstructor
// public class OrderApi {
//
//     private final SysSearchService searchService;
//
//     @PostMapping("init")
//     @ApiOperation("订单查询")
//     public R<?> init() throws InterruptedException {
//         searchService.init();
//         return R.success();
//     }
//
// }
