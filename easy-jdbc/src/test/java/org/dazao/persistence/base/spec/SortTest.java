package org.dazao.persistence.base.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.dazao.persistence.base.spec.Sort;
import org.dazao.persistence.base.spec.Spec;
import org.junit.Test;

public class SortTest {

    @Test
    public void testAsc() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec).asc("name");
        assertThat(sort.buildSqlSort()).isEqualTo(" order by `name`");
        assertThat(spec.getFilterParams()).isEmpty();
    }

    @Test
    public void testDesc() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec).desc("name");
        assertThat(sort.buildSqlSort()).isEqualTo(" order by `name` desc");
    }

    @Test
    public void testEnd() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec);
        sort.asc("name").desc("age").end();
        assertThat(sort.buildSqlSort()).isEqualTo(" order by `name`,`age` desc");
    }

    @Test
    public void testCleanValues() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec);
        sort.desc("name").asc("age").cleanValues();
        assertThat(sort.buildSqlSort()).isEmpty();
    }

    @Test
    public void testIsEmpty() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec);
        sort.desc("name").isEmpty();
        assertThat(sort.buildSqlSort()).isEqualTo(" order by `name` desc");
    }

    @Test
    public void testCloneSpec() {
        Spec spec = new Spec();
        Sort sort = new Sort(spec).asc("name").desc("abc");
        Sort sort2 = sort.clone(spec);
        assertThat(sort == sort2).isFalse();
        assertThat(sort.buildSqlSort()).isEqualTo(sort2.buildSqlSort());
        assertThat(sort2.buildSqlSort()).isEqualTo(sort.buildSqlSort());
    }
}
