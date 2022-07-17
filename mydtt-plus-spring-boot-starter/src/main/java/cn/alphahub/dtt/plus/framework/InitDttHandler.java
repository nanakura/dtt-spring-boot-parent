package cn.alphahub.dtt.plus.framework;

import cn.alphahub.dtt.plus.config.DttProperties;
import cn.alphahub.dtt.plus.entity.ContextWrapper;
import cn.alphahub.dtt.plus.entity.ModelEntity;
import cn.alphahub.dtt.plus.enums.ParserType;
import cn.alphahub.dtt.plus.framework.annotations.EnableDtt;
import cn.alphahub.dtt.plus.framework.core.DttCommentParser;
import cn.alphahub.dtt.plus.framework.core.ParsedModel;
import cn.alphahub.dtt.plus.util.JacksonUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.alphahub.dtt.plus.config.DttProperties.AllInOneTableProperties;
import static cn.alphahub.dtt.plus.config.DttProperties.DataTypeMappingProperties;
import static cn.alphahub.dtt.plus.constant.Constants.BUILDER_SUFFIX;

/**
 * 初始表处理
 *
 * @author weasley
 * @version 1.0
 * @date 2022/7/10
 */
@Data
@Component
@AutoConfigureAfter({InitDttClient.class})
@ConfigurationPropertiesScan({"cn.alphahub.dtt.plus.config"})
@EnableConfigurationProperties({DttProperties.class, DataTypeMappingProperties.class, AllInOneTableProperties.class})
@ConditionalOnBean(annotation = {EnableDtt.class})
public class InitDttHandler implements ApplicationRunner {
    /**
     * 域模型集合, 默认大小：512
     */
    private static final Set<ParsedModel<ModelEntity>> MODEL_ENTITIES = new LinkedHashSet<>(512);
    private static final Logger logger = LoggerFactory.getLogger(InitDttHandler.class);

    @Autowired
    private ClassPathScanningProvider classPathScanningProvider;

    @Autowired
    private ContextWrapper contextWrapper;

    @Autowired
    private AllInOneTableProperties allInOneProperties;

    /**
     * 获取{@code  @EnableDtt}注解
     *
     * @return {@code  @EnableDtt}注解
     */
    public static EnableDtt getEnableDtt() {
        Map<String, Object> beans = SpringUtil.getApplicationContext().getBeansWithAnnotation(SpringBootApplication.class);
        List<EnableDtt> enableDttAnnoSet = new LinkedList<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(beans)) {
            beans.forEach((key, value) -> {
                EnableDtt annotation = value.getClass().getAnnotation(EnableDtt.class);
                if (null != annotation) {
                    enableDttAnnoSet.add(annotation);
                }
            });
        }
        return enableDttAnnoSet.get(0);
    }

    /**
     * 解析注解, 解析模型数据
     *
     * @param dtt 启动自动创建数据库表注解
     */
    public void resolveAnnotationsClass(EnableDtt dtt) {
        // 解析注释, 自动推断实现
        DttCommentParser<ModelEntity> commentParser = contextWrapper.getCommentParser();

        Consumer<Class<?>> classConsumer = aClass -> MODEL_ENTITIES.add(commentParser.parse(aClass.getName()));

        if (ObjectUtils.isNotEmpty(dtt.scanBasePackages())) {
            Set<Class<?>> fullyClasses = classPathScanningProvider.scanBasePackage(dtt.scanBasePackages());
            //Filter out class objects in builder mode
            Set<Class<?>> purelyClasses = fullyClasses.stream().filter(aClass -> !aClass.getSimpleName().endsWith(BUILDER_SUFFIX)).collect(Collectors.toSet());
            purelyClasses.forEach(classConsumer);
        }

        if (ObjectUtils.isNotEmpty(dtt.scanBaseClasses())) {
            Set<Class<?>> classes = Arrays.stream(dtt.scanBaseClasses()).filter(aClass -> !aClass.getSimpleName().endsWith(BUILDER_SUFFIX)).collect(Collectors.toSet());
            classes.forEach(classConsumer);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        URL location = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        if (ResourceUtils.isJarURL(location) && getEnableDtt().parserType() == ParserType.JAVA_DOC) {
            if (logger.isErrorEnabled()) {
                logger.error("You application run with type: '{}',ParserType Of JAVA_DOC not support, Please check your @EnableDtt annotation's configurations.", location);
            }
            throw new UnsupportedOperationException("You application run with type: '" + location + "',ParserType Of JAVA_DOC not support, Please check your @EnableDtt annotation's configurations.");
        }
        this.resolveAnnotationsClass(getEnableDtt());
        if (CollectionUtils.isEmpty(MODEL_ENTITIES)) {
            if (logger.isErrorEnabled()) {
                logger.warn("MODEL_ENTITIES is empty. DTT cannot parse.");
            }
            return;
        }
        if (allInOneProperties.getEnable().equals(true)) {
            String allInOneTables = contextWrapper.getTableHandler().tableAllInOne(MODEL_ENTITIES);
            try (FileOutputStream fos = new FileOutputStream(allInOneProperties.getAbsoluteFilename(), false)) {
                fos.write(allInOneTables.getBytes());
            }
        } else contextWrapper.getTableHandler().bulkOps(MODEL_ENTITIES);

        contextWrapper.getDttRunDetail().setDttEndTime(LocalDateTime.now());
        if (logger.isInfoEnabled() && allInOneProperties.getEnable().equals(true))
            logger.info("Auto created '{}' tables for '{}' seconds. detail: {}, location: {}", MODEL_ENTITIES.size(), LocalDateTimeUtil.between(contextWrapper.getDttRunDetail().getDttStartTime(), contextWrapper.getDttRunDetail().getDttEndTime(), ChronoUnit.SECONDS), JacksonUtil.toJson(contextWrapper.getDttRunDetail()), allInOneProperties.getAbsoluteFilename());
        else if (logger.isInfoEnabled() && allInOneProperties.getEnable().equals(false))
            logger.info("Auto created '{}' tables for '{}' seconds. detail: {}", MODEL_ENTITIES.size(), LocalDateTimeUtil.between(contextWrapper.getDttRunDetail().getDttStartTime(), contextWrapper.getDttRunDetail().getDttEndTime(), ChronoUnit.SECONDS), JacksonUtil.toJson(contextWrapper.getDttRunDetail()));
    }
}