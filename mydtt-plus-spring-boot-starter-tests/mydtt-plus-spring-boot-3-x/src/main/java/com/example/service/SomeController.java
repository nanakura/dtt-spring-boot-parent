package com.example.service;

import com.example.domain.dtt.DttMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Some controller
 */
@RestController
@RequestMapping("/api/member")
public class SomeController {
    @Autowired
    private DttMemberService memberService;

    @PostMapping("/save")
    @Transactional(rollbackFor = {Exception.class})
    public ResponseEntity<Boolean> save(@RequestBody DttMember member) {
        boolean save = memberService.save(member);
        return ResponseEntity.ok(save);
    }

    @PutMapping("/update")
    @Transactional(rollbackFor = {Exception.class})
    public ResponseEntity<Boolean> update(@RequestBody DttMember member) {
        boolean updated = memberService.updateById(member);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<DttMember> info(@PathVariable Long id) {
        DttMember dttMember = memberService.getById(id);
        return ResponseEntity.ok(dttMember);
    }

    @GetMapping("/list/{ids}")
    public ResponseEntity<List<DttMember>> list(@PathVariable Long[] ids) {
        List<DttMember> list = memberService.listByIds(Arrays.asList(ids));
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/delete/{ids}")
    @Transactional(rollbackFor = {Exception.class})
    public ResponseEntity<Boolean> delete(@PathVariable Long[] ids) {
        boolean removed = memberService.removeByIds(Arrays.asList(ids));
        return ResponseEntity.ok(removed);
    }
}