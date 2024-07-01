package vn.teca.digitization.migrate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class PagingUtil {

    public static Pageable buildPageable(int page, int size, List<String> sorts) throws Exception {
        Pageable pageRequest;
        Sort sort = null;
        if (!CollectionUtils.isEmpty(sorts)) {
            for (String str : sorts) {
                String[] array = str.trim().split("\\s*:\\s*");
                Sort.Direction direction = Sort.Direction.fromString(array[1].toUpperCase());
                if (sort == null) {
                    sort = Sort.by(new Sort.Order(direction, array[0]));
                } else {
                    sort.and(direction == Sort.Direction.ASC ? Sort.by(array[0]).ascending() : Sort.by(array[0]).descending());
                }
            }

            pageRequest = PageRequest.of(page, size, sort);
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        return pageRequest;
    }
}
