package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.Permission;
import com.ict.system.repository.v2.PermissionRepository;
import com.ict.system.service.v2.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("permissionServiceV2")
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    @Qualifier("permissionRepositoryV2")
    private PermissionRepository permissionRepository;

    @Override
    public Optional<Permission> findById(Long id) {
        log.info("通过ID查找权限: {}", id);
        return permissionRepository.findById(id);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        log.info("通过名称查找权限: {}", name);
        return permissionRepository.findByName(name);
    }

    @Override
    public List<Permission> findAll() {
        log.info("查找所有权限");
        return permissionRepository.findAll();
    }

    @Override
    public List<Permission> findByGroup(String group) {
        log.info("通过分组查找权限: {}", group);
        return permissionRepository.findByGroup(group);
    }

    @Override
    @Transactional
    public Permission save(Permission permission) {
        log.info("保存权限: {}", permission.getName());
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void delete(Permission permission) {
        log.info("删除权限: {}, ID: {}", permission.getName(), permission.getId());
        permissionRepository.delete(permission);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("删除权限, ID: {}", id);
        permissionRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    @Override
    public long count() {
        return permissionRepository.count();
    }
}
