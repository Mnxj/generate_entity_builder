# GenerateEntityBuilder



<!-- Plugin description -->
This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.

Plug-ins are designed to reduce rework.

This plugin can be found by pressing Control + Enter or Command +N while your cursor is selecting a class.

Note that the selected directory must contain "com", otherwise the file will not be produced

It will continue to be updated...
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  GenerateEntityBuilder"</kbd> >
  <kbd>Install Plugin</kbd>
  

---
Plugin based on the [IntelliJ Platform Plugin Template][template].


This plugin will help you quickly create entityBuilder by control+ Enter and selecting the name of our plugin.<br>
Steps:<br>
1. Select a class with the cursor. Press Control + Enter or command +N<br>
2. For the first time, you can enter the directory /users/... on the left. ./ "or select a directory.<br>
3. click OK, you can view the status through the information in the lower right corner.<br>

        Features:<br>
        1. Different projects can back up different address information.<br>
        2. Multiple dependencies can also be handled.<br>
        3. You can select or enter a directory.<br>
        4. Handles collection dependencies。<br>
        5.You can import properties of inheritance relationships<br>
        6.Can automatically import the packages<br>
        7.Overrides all collection processing<br>
    <br><br
   Thanks to Weiwei's pair.<br>
    Thank you xu Ge for your feedback.

## 时间线
  ### 2021-11-13
  1、设计UI界面<br>
  2、可以根据自己所在项目输入开始包的信息：如我们大多是com.project.dto 只需要输入com，可以让我们方便定位，并为您自动导入包<br>

  ### 2021-11-12
  1、可以自动导入包<br>
  2、对导入的包规范化输出<br>
  3、处理多重依赖import导入多余包的问题<br>
  
  ### 2021-11-11
  1、解决插件只能执行一次，后面宕机的情况<br>
  2、增加可以按照项目地址信息<br>
  3、可以生成集合中的对象<br>
  
  ### 2021-11-10
  1、可以保存地址信息和输入地址信息<br>
  2、增加对多重依赖的功能拓展<br>

  ### 2021-11-09
  1、加入界面界面功能可以手动选择生成的目录地址<br>
  2、能够生成对应的entityBuilder文件<br>
  
  ### 2021-11-08
  1、项目构建，在指定的entityBuilder文件里面可以自动生成with方法<br>

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
