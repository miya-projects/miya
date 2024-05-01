package com.miya.common.config.xlsx;

import org.jxls.builder.JxlsOutput;
import org.jxls.builder.JxlsTemplateFiller;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class XlsxUtil {

    /**
     * 根据模板和数据生成Excel
     * @param inputStream   模板输入流
     * @param outputStream  最终excel输出流
     * @param data          填充数据
     */
    public static void export(InputStream inputStream, OutputStream outputStream, Map<String, Object> data) throws IOException {
        JxlsPoiTemplateFillerBuilder fillerBuilder = JxlsPoiTemplateFillerBuilder.newInstance();
        JxlsTemplateFiller templateFiller = fillerBuilder.withTemplate(inputStream)
                .withExpressionEvaluatorFactory(new CustomExpressionEvaluatorFactory())
                .build();

        templateFiller.fill(data, new JxlsOutput() {
            @Override
            public OutputStream getOutputStream() throws IOException {
                return outputStream;
            }
        });

    }
}
