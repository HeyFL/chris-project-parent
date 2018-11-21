
package util;

import domain.Student;
import domain.StudentDto;
import org.chris.common.utils.Jdk8LambdaUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caizq
 * @date 2018/8/30
 * @since v1.0.0
 */
public class Jdk8LambdaUtilTest {
    /**
     * 取并集测试
     * 1.测试同类型取并集
     * 2.测试不同类型取并集
     */
    @Test
    public void TestGetIntersectionByIntegerParm() {
        Student student1 = new Student();
        student1.setAge(123);
        Student parcelQuery2 = new Student();
        parcelQuery2.setAge(234);
        List<Student> list1 = new ArrayList<>();
        list1.add(student1);
        list1.add(parcelQuery2);


        StudentDto studentDto3 = new StudentDto();
        studentDto3.setAge(123);
        StudentDto studentDto4 = new StudentDto();
        studentDto4.setAge(333);
        List<StudentDto> list2 = new ArrayList<>();
        list2.add(studentDto3);
        list2.add(studentDto4);

        List difTypeIntegerList = Jdk8LambdaUtil.getIntersectionByIntegerParm(list1, Student::getAge, list2, StudentDto::getAge);
        List sameTypeIntegerList = Jdk8LambdaUtil.getIntersectionByIntegerParm(list1, Student::getAge, list1, Student::getAge);

        assert (difTypeIntegerList.size() == 1);
        assert (sameTypeIntegerList.size() == 2);
    }
}
