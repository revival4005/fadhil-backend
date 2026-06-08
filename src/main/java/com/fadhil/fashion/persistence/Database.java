package com.fadhil.fashion.persistence;

import com.fadhil.fashion.model.ContactMessage;
import com.fadhil.fashion.model.OrderRequest;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private List<UserRecord> users = new ArrayList<>();
    private List<OrderRequest> orders = new ArrayList<>();
    private List<ContactMessage> contacts = new ArrayList<>();

    public List<UserRecord> getUsers() {
        return users;
    }

    public void setUsers(List<UserRecord> users) {
        this.users = users;
    }

    public List<OrderRequest> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderRequest> orders) {
        this.orders = orders;
    }

    public List<ContactMessage> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactMessage> contacts) {
        this.contacts = contacts;
    }
}
