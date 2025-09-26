import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.hetao.FlowableServer;

import java.util.List;

@SpringBootTest(classes = FlowableServer.class)
public class FlowableTest {
    /**
     * 部署流程相关
     */
    @Autowired
    private RepositoryService repositoryService;
    /**
     * 创建实例相关
     */
    @Autowired
    private RuntimeService runtimeService;
    /**
     * 任务相关
     */
    @Autowired
    private TaskService taskService;
    @Test
    public void test() {
        //启动流程
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("first.bpmn20.xml")
                .name("第一个部署流程")
                .deploy();

        repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        System.out.println("流程id:" + deployment.getId());
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId())
                .singleResult();
        System.out.println("流程定义id："+processDefinition.getId());
        //创建实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        System.out.println("流程实例id:" + processInstance.getId());
        //查询任务
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        //完成任务
        list.forEach(task -> {
            System.out.println("任务id:" + task.getId());
            System.out.println("任务名称:" + task.getName());
            System.out.println("任务创建时间:" + task.getCreateTime());
            System.out.println("任务办理人:" + task.getAssignee());
            taskService.complete(task.getId());
            System.out.println("完成任务:" + task.getName());
        });
    }
}
