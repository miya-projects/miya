package com.miya.common.config.xlsx;

import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XlsxUtil {

    public static void export(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        final JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        final Transformer transformer = jxlsHelper.createTransformer(inputStream, outputStream);
        transformer.getTransformationConfig().setExpressionEvaluator(new CustomExpressionEvaluator());
        jxlsHelper.processTemplate(context, transformer);
    }
}
