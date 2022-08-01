package cn.alphahub.dtt.plus.framework.core;

import cn.alphahub.dtt.plus.config.datamapper.SqlserverDataMapperProperties;
import cn.alphahub.dtt.plus.entity.ContextWrapper;
import cn.alphahub.dtt.plus.entity.ModelEntity;
import cn.alphahub.dtt.plus.framework.annotations.EnableDtt;
import cn.alphahub.dtt.plus.util.JacksonUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * sqlserver默认建表实现
 *
 * @author weasley
 * @version 1.0
 * @date 2022/7/10
 */
@Component
@ConditionalOnBean(annotation = {EnableDtt.class})
public class DefaultSqlserverTableHandler extends DttAggregationRunner implements DttTableHandler<ModelEntity> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSqlserverTableHandler.class);
    @Autowired
    private SqlserverDataMapperProperties sqlserverDataMapperProperties;

    @Override
    public String create(ParseFactory<ModelEntity> parseFactory) {
        ModelEntity model = parseFactory.getModel();
        if (logger.isInfoEnabled())
            logger.info("使用sqlserver默认建表实现 {}", JacksonUtil.toJson(parseFactory.getModel()));
        if (null == model || CollectionUtils.isEmpty(model.getDetails())) {
            logger.warn("sqlserver的表结构元数据解析结果不能为空 {}", model);
            return null;
        }

        ContextWrapper commentParser = SpringUtil.getBean(ContextWrapper.class);
        Properties mappingProperties = sqlserverDataMapperProperties.getMappingProperties();
        model.getDetails().forEach(detail -> {
            if (Objects.equals(Enum.class.getSimpleName(), detail.getJavaDataType())) {
                String actuallyDbDataType = commentParser.getCommentParser().deduceDbDataTypeWithLength(detail.getFiledName());
                handlingEnumerationTypeToString(mappingProperties, detail, actuallyDbDataType);
            }
            if (Objects.equals(Boolean.class.getSimpleName(), detail.getJavaDataType())) {
                detail.setInitialValue(Boolean.parseBoolean(detail.getInitialValue()) ? "1" : "0");
            }
        });

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("defaultCollate", getDefaultCollate(sqlserverDataMapperProperties));
        String template = resolve(() -> model, velocityContext);
        String[] pureSQL = template.split("GO\n");
        batchExecute(pureSQL);
        return template;
    }


    /**
     * Get default collate
     * <p>
     * Solve Chinese garbled characters, <a href="https://docs.microsoft.com/zh-cn/sql/relational-databases/collations/collation-and-unicode-support?view=sql-server-ver16">Here is the help link for SQLServer official.</a>
     *
     * @param properties The mapper of Java data type mapping with sqlserver
     * @return collate
     */
    private String getDefaultCollate(SqlserverDataMapperProperties properties) {
        Locale locale = Locale.getDefault();
        List<String> simplifiedChinese = Arrays.asList("zh", "zh-CN", "zh-Hans", "zh-Hans-CN");
        List<String> traditionalChinese = Arrays.asList("zh-HK", "zh-TW", "zh-Hans-HK", "zh-Hans-TW");
        if (simplifiedChinese.contains(locale.toLanguageTag())) return "Chinese_PRC_CI_AS";
        if (traditionalChinese.contains(locale.toLanguageTag())) return "Chinese_Taiwan_Stroke_CI_AS";
        return properties.getDefaultCollate();
    }
}
