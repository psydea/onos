java_library(
    name = "controllflow",
)

COMPILE_DEPS = CORE_DEPS + KRYO + CLI + [
    "//core/store/serializers:onos-core-serializers",
    "//utils/rest:onlab-rest",
    "@javax_ws_rs_api//jar",
    "@jackson_databind//jar"
]

osgi_jar_with_tests(
    suppress_checkstyle = True,
    karaf_command_packages = ["org.shihyanproject.controllflow"],
    resources = glob(["src/main/resources/**"]),
    resources_root = "src/main/resources",
    web_context = "/onos/v1/controllflow",
    deps = COMPILE_DEPS,
)

onos_app(
    category = "Test",
    description = "Controll Flow",
    title = "Test build",
    url = "http://onosproject.org",
)
