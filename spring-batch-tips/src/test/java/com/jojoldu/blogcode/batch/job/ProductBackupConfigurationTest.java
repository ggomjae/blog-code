package com.jojoldu.blogcode.batch.job;

import com.jojoldu.blogcode.batch.TestBatchConfig;
import com.jojoldu.blogcode.batch.domain.Product;
import com.jojoldu.blogcode.batch.domain.ProductBackup;
import com.jojoldu.blogcode.batch.domain.ProductBackupRepository;
import com.jojoldu.blogcode.batch.domain.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.jojoldu.blogcode.batch.config.DataSourceConfiguration.OTHER_DATASOURCE;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jojoldu@gmail.com on 20/01/2020
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestBatchConfig.class, ProductBackupConfiguration.class})
@SpringBatchTest
public class ProductBackupConfigurationTest {
    public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBackupRepository productBackupRepository;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier(OTHER_DATASOURCE)
    private DataSource otherDataSource;

    private JdbcTemplate otherJdbcTemplate;

    @BeforeEach
    void setUp() {
        this.otherJdbcTemplate = new JdbcTemplate(otherDataSource);
    }

    @AfterEach
    public void after() throws Exception {
        otherJdbcTemplate.execute("DELETE FROM product");
        productBackupRepository.deleteAllInBatch();
    }

    @Test
    public void Product가_ProductBackup으로_이관된다() throws Exception {
        //given
        LocalDate txDate = LocalDate.of(2020,10,12);
        String name = "a";
        int categoryNo = 1;
        int expected1 = 1000;
        int expected2 = 2000;
        otherJdbcTemplate.update("insert into product (name, price, category_no, create_date) values (?, ?, ?, ?)", name, expected1, categoryNo, txDate);
        otherJdbcTemplate.update("insert into product (name, price, category_no, create_date) values (?, ?, ?, ?)", name, expected2, categoryNo, txDate);

        List<Map<String, Object>> others = otherJdbcTemplate.queryForList("select * from product");
        List<Product> mains = productRepository.findAll();

        JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("txDate", txDate.format(FORMATTER))
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<ProductBackup> backups = productBackupRepository.findAll();
        assertThat(backups.size()).isEqualTo(2);
    }
}
