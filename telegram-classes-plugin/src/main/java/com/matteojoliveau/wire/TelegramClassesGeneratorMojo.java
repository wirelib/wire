package com.matteojoliveau.wire;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.matteojoliveau.wire.scraper.TelegramScraper;
import com.matteojoliveau.wire.scraper.TemplateModel;
import freemarker.core.UndefinedOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Goal which generate Kotlin classes from Telegram documentation.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TelegramClassesGeneratorMojo
        extends AbstractMojo {

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File outputDirectory;

    public void execute()
            throws MojoExecutionException {
        try {
            final File f = outputDirectory;

            final Template template;
            final String templateFile = "data_class.ftlh";
            final Configuration cfg = configureFreemarker();
            try {
                template = cfg.getTemplate(templateFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Error reading template file " + templateFile, e);
            }

            if (!f.exists()) {
                final boolean success = f.mkdirs();
            }

            final File gen = new File(f, "generated-sources/telegram/org/telegram");

            if (!gen.exists()) {
                final boolean success = gen.mkdirs();
            }

            final TelegramScraper scraper = new TelegramScraper();
            final List<TemplateModel> models = scraper.scrape();

            for (TemplateModel model : models) {

                processTemplate(template, gen, model);
            }

            final Template response = cfg.getTemplate("response.ftlh");
            processTemplate(response, gen, new TemplateModel("", Collections.singletonList(new HashMap<>()), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTemplate(Template template, File gen, TemplateModel model) throws MojoExecutionException {
        FileWriter w = null;
        final String className = String.format("%s.kt", model.getTitle());
        final File modelFile = new File(gen, className);
        try {
            w = new FileWriter(modelFile);

            template.process(model, w);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + modelFile.getName(), e);
        } catch (TemplateException e) {
            throw new MojoExecutionException("Error writing template" + className, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private Configuration configureFreemarker() throws IOException {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        cfg.setWrapUncheckedExceptions(true);
        cfg.setLogTemplateExceptions(false);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setOutputFormat(UndefinedOutputFormat.INSTANCE);
        return cfg;
    }
}
