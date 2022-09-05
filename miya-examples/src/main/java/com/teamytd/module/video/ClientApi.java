package com.teamytd.module.video;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.R;
import com.miya.system.module.download.DownloadTask;
import com.miya.system.module.download.SimpleDownloadTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

//todo 测试后删除
@RequestMapping(value = "client")
@RestController
@Slf4j
@Api(tags = {"客户服务"})
@Acl(userType = Acl.NotNeedLogin.class)
@Validated
@RequiredArgsConstructor
public class ClientApi {

    public void df(){
        DownloadTask task = new SimpleDownloadTask("a.xls") {
            @Override
            public InputStream get() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

        };
    }

    @PostMapping("testConvert")
    @ApiOperation("test")
    public R<?> testConvert(@Validated TestDTO dto) {
        log.info("\n" + dto.toString());
        return R.success();
    }

    @PostMapping("testConvertRequestBody")
    @ApiOperation("testConvertRequestBody")
    public R<?> testConvertRequestBody(@Validated @RequestBody  TestDTO dto) {
         // log.info("\n" + dto.toString());
        return R.successWithData(dto);
    }

    @Getter
    @Setter
    @ToString
    static class TestDTO{
        private String str;
        private int num;
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
