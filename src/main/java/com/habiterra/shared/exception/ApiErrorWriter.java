package com.habiterra.shared.exception;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
@Component
public class ApiErrorWriter {
    private final ObjectMapper mapper;
    public ApiErrorWriter(ObjectMapper mapper){this.mapper=mapper;}
    public ApiError body(int status,String code,String message,String path){
        return new ApiError(Instant.now(),status,HttpStatus.valueOf(status).name(),code,message,path);
    }
    public void write(HttpServletRequest request,HttpServletResponse response,int status,String code,String message)throws IOException {
        response.setStatus(status);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
        if(status==401)response.setHeader("WWW-Authenticate","Bearer");
        mapper.writeValue(response.getOutputStream(),body(status,code,message,request.getRequestURI()));
    }
}
