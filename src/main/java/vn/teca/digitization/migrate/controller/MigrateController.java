package vn.teca.digitization.migrate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import vn.teca.digitization.migrate.service.MigrateService;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/migrate")
public class MigrateController {

    private final MigrateService migrateService;

    @PostMapping("/processing")
    public String processing(@RequestBody String jsonUpload) throws Exception {
        //Lấy nội dung file dưới dạng json string
        // tải dữ liệu đầu vào json và chuển thành jsonNode
        ObjectMapper mapper = new ObjectMapper();
//        String dauVaoTuApi = Files.readString(Paths.get("D:\\Projects\\XuLyJson\\api.json"));
        JsonNode rootNodeDauVao = mapper.readTree(jsonUpload);
        // tải dữ liệu đầu vào cấu hình đọc json
        StringBuilder content = new StringBuilder();
        try {
            File myObj = new File("config.json");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                content.append(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        JsonNode rootNodeCauHinh = mapper.readTree(content.toString());
        // map id lưu tạm
        Map<String, String> tempsData = new HashMap<>();
        // đọc cấu hình
        for (int i = 0; i < rootNodeCauHinh.size(); i++) {
            // Xử lý bảng ca nhan
            JsonNode elementRoot = rootNodeCauHinh.get(i);
            String tableName = elementRoot.get("table_name").textValue(); // ten bang
            Integer rowsNumber = elementRoot.get("rowsNumber").intValue(); // so dong du lieu duoc tao ra
            for (int j = 0; j < rowsNumber; j++) {
                // xu ly data
                migrateService.processing(tableName, rootNodeDauVao, elementRoot.get("data").get(j), elementRoot.get("columns"), tempsData);
            }
        }
        return "OK";
    }
}
