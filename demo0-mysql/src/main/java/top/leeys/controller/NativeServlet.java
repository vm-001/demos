package top.leeys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import top.leeys.pojo.Message;
import top.leeys.server.MainService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NativeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Autowired
    private MainService mainService;
    private ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        if (type != null) {
            //简单起见都没做异常的逻辑处理
            switch (type) {
                // 2600-2700
                case "update":
                    mainService.testNativeUpdate(mainService.getRandomCourseCode());
                    response.getWriter().print(mapper.writeValueAsString(new Message(0, "update success")));
                    break;
                // 3300
                case "select":
                    response.getWriter().print(mapper.writeValueAsString(new Message(0, "select success")));
                    mainService.testNativeSelect();
                    break;
                case "procedure":
                    break;
            }
        }

    }
}
