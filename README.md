# 📊 Expense Tracker App  

A secure and user-friendly **Expense Tracker App** built with **Kotlin + XML**, designed to help users manage, categorize, and analyze their expenses with simplicity and privacy in mind.  

---

## 🚀 Features  

### 🔑 Security  
- Set up a **4-digit PIN** during first use.  
- PIN required to **add, edit, or delete** any expense.  

### 💰 Expense Management  
- Add expense with fields: Paid To, Amount, Date (auto-fills to today), Category, Remark (optional).  
- All expenses are stored in categorized lists.  

### 📂 Categories & Sorting  
- Predefined categories: **Food, Travel, Household, Others**.  
- Sorting options: **Low → High, High → Low, Newest, Oldest**.  

### 📊 Dashboard  
- **Pie chart** visualization of expenses by category (color-coded).  
- Category-wise percentage labels.  
- Expense summary table with **total + category breakdown**.  

### 👤 Profile  
- First-time setup: enter **name** (displayed on profile card).  
- Add a **profile picture** (persists until app uninstall).  
- **Theme toggle**: Light ↔ Dark.  
- **Erase Expenses** (only expenses) or **Delete Account** (all data including profile & PIN).  

### 📞 Contact Us  
- Reach support via: **Mail, Discord, Call, WhatsApp**.  

---

## 🖼 App Navigation Flow  
- **Home** → Recent expenses  
- **Bottom Nav** →  
  `Category | Dashboard | Add (center) | Profile | Contact Us`  

---

## 🛠 Tech Stack  
- **Language**: Kotlin  
- **UI**: XML Layouts  
- **Persistence**: Local database (Room / SQLite)  
- **Visualization**: Pie Chart library  
- **Design**: Clean & minimal, with secure flows  
