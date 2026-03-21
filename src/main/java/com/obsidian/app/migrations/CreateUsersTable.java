package com.obsidian.app.migrations;

import com.obsidian.core.database.Migration;

/**
 * Creates the "users" table required by the authentication system.
 * Run automatically by the migration runner on first deploy.
 */
public class CreateUsersTable extends Migration
{
    @Override
    public void up() {
        createTable("users", table -> {
            table.id();
            table.string("username").notNull().unique();
            table.string("password").notNull();
            table.string("role").notNull().defaultValue("USER");
            table.timestamps();
        });
    }

    /**
     * Drops the "users" table. Used when rolling back this migration.
     */
    @Override
    public void down() {
        dropTable("users");
    }
}
