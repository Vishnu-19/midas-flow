package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;
import com.jpmc.midascore.repository.TransactionRepository;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    public void save(Transaction transaction) {

        UserRecord sender = queryForUser(transaction.getSenderId());
        UserRecord receiver = queryForUser(transaction.getRecipientId());
        TransactionRecord transactionRecord = new TransactionRecord(sender, receiver, transaction.getAmount(), transaction.getIncentive());
        transactionRepository.save(transactionRecord);
        
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        receiver.setBalance(receiver.getBalance() + transaction.getAmount() + transaction.getIncentive());
        save(sender);
        save(receiver);
    }

    public boolean isValidTransaction(Transaction transaction){
        UserRecord sender = queryForUser(transaction.getSenderId());
        if(sender == null)
            return false;
        UserRecord receiver = queryForUser(transaction.getRecipientId());
        if (receiver == null)
            return false;

        if(sender.getBalance() < transaction.getAmount())
            return false;

        return true;
    }

    private UserRecord queryForUser(long userId) {
        return userRepository.findById(userId);
    }

    public float queryUserBalance(Long userId) {
        UserRecord userRecord = queryForUser(userId);
        if (userRecord == null) {
            return 0;
        } else {
            return userRecord.getBalance();
        }
    }
}
