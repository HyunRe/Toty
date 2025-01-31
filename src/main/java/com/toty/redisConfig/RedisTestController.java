package com.toty.redisConfig;

import java.time.Duration;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/redis/singleData")
public class RedisTestController {

    private final RedisService redisSingleDataService;

    public RedisTestController(RedisService redisSingleDataService) {
        this.redisSingleDataService = redisSingleDataService;
    }

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 조회합니다.
     *
     */
    @PostMapping("/getValue")
    public ResponseEntity<Object> getValue(@RequestBody RedisDto redisDto) {
        String result = redisSingleDataService.getData(redisDto.getKey());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Redis 단일 데이터 값을 등록/수정합니다.(duration 값이 존재하면 메모리 상 유효시간을 지정합니다.)
     *
     */
    @PostMapping("/setValue")
    public ResponseEntity<Object> setValue(@RequestBody RedisDto redisDto) {
        int result = 0;
        if (redisDto.getDuration() == null) {
            result = redisSingleDataService.setData(redisDto.getKey(), redisDto.getValue());
        } else {
            result = redisSingleDataService.setData(redisDto.getKey(), redisDto.getValue(), redisDto.getDuration());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 삭제합니다.
     *
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteRow(@RequestBody RedisDto redisDto) {
        int result = redisSingleDataService.deleteData(redisDto.getKey());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
