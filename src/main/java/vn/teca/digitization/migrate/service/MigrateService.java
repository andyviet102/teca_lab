package vn.teca.digitization.migrate.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrateService extends BaseCommonService {

    @Autowired
    DataSource dataSource;

    private String getValue(String key, JsonNode jsonNode) {
        if (key == null || key.isEmpty() || jsonNode == null) return null;
        String[] keys = key.split("\\.");
        JsonNode node = jsonNode;
        for (String k : keys) {
            if (k.contains("[")) {
                Pattern pattern = Pattern.compile("\\[.+\\]");
                Matcher matcher = pattern.matcher(k);
                node = node.get(k.split("\\[")[0]);
                while (matcher.find()) {
                    String index = matcher.group().replace("[", "").replace("]", "");
                    node = node.get(Integer.parseInt(index));
                }
            } else {
                node = node.get(k);
            }
            if (node == null) return null;
        }
        return node.asText();
    }


    public void processing(String tableName, JsonNode rootNode, JsonNode nodeData, JsonNode nodeColumns, Map<String, String> tempsData) throws Exception {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        String idRow = ""; // id dòng
        for (Iterator<String> it = nodeData.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            if (Objects.equals(key, "key")) {
                // id cua FE map
                idRow = nodeData.get(key).textValue();
            } else {
                columns.add(key);
                if (Objects.equals(key, "id")) {
                    // sinh id của riêng bản ghi
                    String id = UUID.randomUUID().toString();
                    tempsData.put(tableName + "_" + idRow, id);
                    values.add(id);
                } else {
                    // xử lý dữ liệu với các type khác nhau
                    int typeValue = getColumInfo(key, nodeColumns).get("type").intValue();
                    // loại du lieu co type 1 luu theo cau truc json dau vao
                    if (typeValue == 1) {
                        String typeJava = getColumInfo(key, nodeColumns).get("typeJava").textValue();
                        String valueOutput = getValue(nodeData.get(key).textValue(), rootNode);
                        switch (typeJava) {
                            case "Long":
                                values.add(Long.parseLong(valueOutput));
                                break;
                            case "Integer":
                                values.add(Integer.parseInt(valueOutput));
                                break;
                            case "date":
                                String format = getColumInfo(key, nodeColumns).get("format").textValue();
                                SimpleDateFormat sf = new SimpleDateFormat(format);
                                values.add(sf.parse(valueOutput));
                                break;
                            default:
                                values.add(valueOutput);
                                break;
                        }
                    }
                    // 2 loai lay du lieu tu bang da tao roi
                    if (typeValue == 2) {
                        String tableRelationship = getColumInfo(key, nodeColumns).get("table_relationship").textValue();
                        String idRelationship = tempsData.get(tableRelationship + "_" + nodeData.get(key).textValue());
                        values.add(idRelationship);
                    }
                    if (typeValue == 3) {
                        values.add(nodeData.get(key).intValue());
                    }
                }
            }
        }
        insertData(tableName, columns, values);
    }

    private JsonNode getColumInfo(String keyName, JsonNode nodeData) throws Exception {
        for (int j = 0; j < nodeData.size(); j++) {
            if (keyName.equals(nodeData.get(j).get("key").textValue())) {
                return nodeData.get(j);
            }
        }
        throw new Exception("Lỗi cấu trúc file json config không có trường khớp với cột ở Data");
    }

    public void insertData(String tableName, List<String> columns, List<Object> values) throws SQLException {
        // Lấy kết nối
        Connection conn = dataSource.getConnection();
        // Tạo câu lệnh SQL
        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (";
        for (int i = 0; i < columns.size(); i++) {
            sql += "?, ";
        }
        sql = sql.substring(0, sql.length() - 2) + ")";

        // Chuẩn bị câu lệnh
        PreparedStatement stmt = conn.prepareStatement(sql);
        // Điền giá trị vào các dấu ?
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof String) {
                stmt.setString(i + 1, (String) value);
            } else if (value instanceof Integer) {
                stmt.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                stmt.setLong(i + 1, (Long) value);
            } else if (value instanceof Date) {
                Date date = (Date) value;
                stmt.setDate(i + 1, new java.sql.Date(date.getTime()));
            } else {
                stmt.setObject(i + 1, value);
            }
        }
        // Thực thi câu lệnh
        stmt.executeUpdate();
        // Đóng kết nối
        stmt.close();
        conn.close();
    }
}
