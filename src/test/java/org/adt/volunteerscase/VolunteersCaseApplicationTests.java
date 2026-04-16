package org.adt.volunteerscase;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class VolunteersCaseApplicationTests {

    @Test
    void mainMethod_shouldBePresentAndStatic() throws NoSuchMethodException {
        Method mainMethod = VolunteersCaseApplication.class.getDeclaredMethod("main", String[].class);

        assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
        assertThat(Modifier.isStatic(mainMethod.getModifiers())).isTrue();
    }

}
