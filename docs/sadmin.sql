SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for actionlog
-- ----------------------------
DROP TABLE IF EXISTS `actionlog`;
CREATE TABLE `actionlog` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作IP信息',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作IP地址',
  `timestamp` bigint NOT NULL COMMENT '操作时间戳',
  `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志标题',
  `request_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求方式',
  `request_params` text COLLATE utf8mb4_general_ci COMMENT '请求参数',
  `response_result` text COLLATE utf8mb4_general_ci COMMENT '响应结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ----------------------------
-- Records of actionlog
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for dept
-- ----------------------------
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '部门名称',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除状态，0未删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- ----------------------------
-- Records of dept
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for loginlog
-- ----------------------------
DROP TABLE IF EXISTS `loginlog`;
CREATE TABLE `loginlog` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `ip` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录IP信息',
  `address` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录IP地址',
  `timestamp` bigint NOT NULL COMMENT '登录时间戳',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- ----------------------------
-- Records of loginlog
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for perms
-- ----------------------------
DROP TABLE IF EXISTS `perms`;
CREATE TABLE `perms` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` int NOT NULL COMMENT '父级权限ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称',
  `identifier` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限标识',
  `path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限路由路径',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限路由组件',
  `type` int NOT NULL COMMENT '权限类型，0目录/1菜单/2操作',
  `sort_id` int NOT NULL DEFAULT '0' COMMENT '排序ID',
  `status` int NOT NULL DEFAULT '1' COMMENT '权限状态，1显示/0隐藏',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除状态，0未删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- ----------------------------
-- Records of perms
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `perms` text COLLATE utf8mb4_general_ci COMMENT '授权的权限菜单列表',
  `remarks` varchar(255) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '-' COMMENT '角色备注',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除状态，0未删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` int NOT NULL COMMENT '关联部门ID',
  `role_id` int NOT NULL COMMENT '关联角色ID',
  `username` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '账户账号',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '账户密码',
  `salts` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '账户盐值',
  `realname` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '真实姓名',
  `remarks` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户备注',
  `status` int NOT NULL COMMENT '账户状态，1正常/0禁用',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除状态，0未删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
