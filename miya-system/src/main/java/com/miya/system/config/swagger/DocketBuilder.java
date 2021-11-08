package com.miya.system.config.swagger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import springfox.documentation.RequestHandler;

import java.util.function.Predicate;

/**
 * 用来创建 Swagger Docket
 */
@RequiredArgsConstructor
@Getter
public class DocketBuilder {

    private final String title;
    private final Predicate<RequestHandler> predicate;
}
