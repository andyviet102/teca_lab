package vn.teca.digitization.file.controller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.teca.digitization.file.service.HitachiFileService;

import java.util.ArrayList;
import java.util.List;

@RestController
@Controller
public class FilesController {
    @Autowired
    private HitachiFileService hitachiFileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files, @RequestParam String path) throws Exception {
        try {
            for (MultipartFile multipartFile : files) {
                byte[] arr = multipartFile.getBytes();
                String path_ = path + multipartFile.getOriginalFilename();
                hitachiFileService.uploadFiles(path_, arr);
            }
            return ResponseEntity.ok().body("OK");
        } catch (Exception e) {
            return ResponseEntity.ok().body(e.toString());
        }
    }

    @GetMapping("/image")
    public ResponseEntity<?> getFileFromPath(@RequestParam String path) throws Exception {
        HttpEntity httpEntity = hitachiFileService.getObject(path);
        Resource resource = new ByteArrayResource(IOUtils.toByteArray(httpEntity.getContent()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(httpEntity.getContentType().getValue()))
                .body(resource);
    }
}

