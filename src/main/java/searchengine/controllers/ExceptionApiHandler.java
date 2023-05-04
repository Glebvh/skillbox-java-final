package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.services.IndexingException;

import java.util.HashMap;

@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(IndexingException.class)
    public ResponseEntity<HashMap<String, Object>> IndexingException(IndexingException exception) {
        HashMap<String, Object> response = new HashMap<>();
        String msg = exception.getMessage();
        response.put("result", false);
        response.put("error", msg);
        if (msg.equals("Ошибка запроса")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (msg.equals("Ошибка авторизации")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (msg.equals("Ошибка доступа")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } else if (msg.equals("Страница не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (msg.equals("Ошибка")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return null;
    }

}