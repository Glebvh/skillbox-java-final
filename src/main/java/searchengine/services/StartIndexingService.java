package searchengine.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public interface StartIndexingService {

    HashMap<String,Object> start();
}
