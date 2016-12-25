package hawkeye.view;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;

import java.io.File;

public class FreemarkerConfiguration {
    private static final String TEMPLATES_DIRECTORY = "src/main/resources/templates";

    @Getter
    private Configuration configuration;

    @Inject
    public FreemarkerConfiguration() throws Exception {
        configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setDirectoryForTemplateLoading(new File(TEMPLATES_DIRECTORY));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }
}
