package com.teamytd.module.video;

import com.miya.common.annotation.Acl;
import com.miya.common.annotation.RequestJsonParam;
import com.miya.common.annotation.constraint.AutoMaxLength;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseForm;
import com.miya.system.module.user.model.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;

//todo 测试后删除
@RequestMapping(value = "client")
@RestController
@Slf4j
@Tag(name = "客户服务")
@Acl(userType = Acl.NotNeedLogin.class)
@Validated
@RequiredArgsConstructor
public class ClientApi {


    @PostMapping("testConvert")
    @Operation(summary = "test")
    public R<?> testConvert(@Validated TestDTO dto) {
        log.info("\n" + dto.toString());
        return R.success();
    }

    @PostMapping("testConvertRequestBody")
    @Operation(summary = "testConvertRequestBody")
    public R<?> testConvertRequestBody(@Validated @RequestBody TestDTO dto) {
         // log.info("\n" + dto.toString());
        return R.successWithData(dto);
    }

    @PostMapping("testRequestJson")
    @Operation(summary = "testRequestJson")
    public R<?> testRequestJson(@RequestJsonParam String id, @RequestJsonParam String name) {
        log.info("\n" + id);
        return R.success();
    }

    @Getter
    @Setter
    @ToString
    @AutoMaxLength(enabled = true)
    static class TestDTO extends BaseForm<SysUser> {
        @NotBlank
        private String name;
        private YearMonth yearMonth;
        private Integer num;
        private Date date;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private Timestamp timestamp;
        private SubDTO dto;
        private SubDTO2 dto2;
    }

    @Getter
    @Setter
    static class SubDTO{
        private String a;
    }

    @Getter
    @Setter
    static class SubDTO2{
        private String a;
    }
}
