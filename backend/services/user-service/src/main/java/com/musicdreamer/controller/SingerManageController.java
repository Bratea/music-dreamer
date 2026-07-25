package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/singer/manage")
@Tag(name = "歌手管理(辅助)", description = "歌手相关辅助接口")
public class SingerManageController {

    private final RoleService roleService;

    public SingerManageController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/is-singer")
    @Operation(summary = "判断当前用户是否是歌手")
    public CommonResult<Map<String, Boolean>> isSinger(@RequestAttribute("userId") Long userId) {
        boolean isSinger = roleService.getRoleCodesByUserId(userId).contains("SINGER");
        Map<String, Boolean> result = new HashMap<>();
        result.put("isSinger", isSinger);
        return CommonResult.success(result);
    }
}
