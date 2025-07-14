#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>

typedef struct sqlite3 sqlite3;

typedef int (*sqlite3_open_t)(const char *filename, sqlite3 **ppDb);
typedef int (*sqlite3_exec_t)(sqlite3*, const char *sql, int (*callback)(void*,int,char**,char**), void *, char **errmsg);
typedef int (*sqlite3_close_t)(sqlite3*);
typedef void (*sqlite3_free_t)(void*);

int main(int argc, char *argv[]) {
    if (argc < 3) {
        fprintf(stderr, "Usage: %s <DATABASE_PATH> <SQL_QUERY>\n", argv[0]);
        return 1;
    }
    const char *db_path = argv[1];
    const char *sql = argv[2];

    void *handle = dlopen("libsqlite.so", RTLD_NOW);
    if (!handle) {
        fprintf(stderr, "Failed to load libsqlite.so: %s\n", dlerror());
        return 1;
    }

    sqlite3_open_t sqlite3_open = (sqlite3_open_t) dlsym(handle, "sqlite3_open");
    sqlite3_exec_t sqlite3_exec = (sqlite3_exec_t) dlsym(handle, "sqlite3_exec");
    sqlite3_close_t sqlite3_close = (sqlite3_close_t) dlsym(handle, "sqlite3_close");
    sqlite3_free_t sqlite3_free = (sqlite3_free_t) dlsym(handle, "sqlite3_free");

    if (!sqlite3_open || !sqlite3_exec || !sqlite3_close || !sqlite3_free) {
        fprintf(stderr, "Missing symbols\n");
        return 1;
    }

    sqlite3 *db = NULL;
    char *err = NULL;

    if (sqlite3_open(db_path, &db) != 0) {
        fprintf(stderr, "Cannot open DB\n");
        return 1;
    }

    if (sqlite3_exec(db, sql, NULL, NULL, &err) != 0) {
        fprintf(stderr, "SQL error: %s\n", err);
        sqlite3_free(err);
        sqlite3_close(db);
        return 1;
    }

    printf("SQL query executed successfully!\n");

    sqlite3_close(db);
    return 0;
}