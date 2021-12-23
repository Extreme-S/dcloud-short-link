package org.example.manager;

import java.util.List;
import org.example.model.AccountDO;

public interface AccountManager {

    int insert(AccountDO accountDO);

    List<AccountDO> findByPhone(String phone);
}
