package com.rbac.util;

import com.rbac.service.component.IPermissionComponent;
import com.rbac.service.PermissionTreeValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * 实验3.3.4：权限缓存优化（本地缓存，5分钟过期）
 */
public class PermissionCache {
    // 缓存结构：用户名 → 缓存实体（权限树+过期时间）
    private Map<String, CacheEntry> userPermCache = new HashMap<>();
    // 缓存有效期：5分钟（300000毫秒）
    private static final long EXPIRE_MS = 5 * 60 * 1000;

    // 缓存实体内部类
    private static class CacheEntry {
        IPermissionComponent permTree; // 用户权限树
        long expireTime; // 过期时间戳

        CacheEntry(IPermissionComponent permTree) {
            this.permTree = permTree;
            this.expireTime = System.currentTimeMillis() + EXPIRE_MS;
        }

        // 判断缓存是否过期
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 获取用户权限树（优先从缓存取，过期则重建）
     */
    public IPermissionComponent getUserPermTree(String username, PermissionTreeValidator validator) {
        // 1. 查缓存
        CacheEntry entry = userPermCache.get(username);
        // 2. 缓存有效 → 直接返回
        if (entry != null && !entry.isExpired()) {
            System.out.println("从缓存获取用户" + username + "的权限树");
            return entry.permTree;
        }
        // 3. 缓存失效/不存在 → 重建并缓存
        System.out.println("缓存失效，重建用户" + username + "的权限树");
        IPermissionComponent newTree = validator.buildRealUserPermissionTree(username);
        userPermCache.put(username, new CacheEntry(newTree));
        return newTree;
    }

    /**
     * 权限变更时，主动清空指定用户的缓存
     */
    public void clearUserCache(String username) {
        userPermCache.remove(username);
        System.out.println("已清空用户" + username + "的权限缓存");
    }

    /**
     * 清空所有缓存（系统刷新时用）
     */
    public void clearAllCache() {
        userPermCache.clear();
        System.out.println("已清空所有权限缓存");
    }
}