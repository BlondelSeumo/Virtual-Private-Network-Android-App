import Layout from "@app/components/layout";
import React from "react";
import UISearch from "@app/components/core/input/search";
import {Dropdown, Menu, Modal, notification, Select} from "antd";
import UIButton from "@app/components/core/button";
import UITable from "@app/components/core/table";
import Tag from "@app/components/core/tag";

import MoreIcon from '@app/resources/images/more.svg'
import {auth, firestore} from "@app/services/firebase";
import {delay, encodeEmail, postDataWithFetch} from "@app/utils";
import {Link} from "react-router-dom";
import {LoadingIcon} from "@app/components/core/loading-icon";
import {LoadingPage2} from "@app/components/core/loading";
import {API} from "@app/request";
import {EMAIL} from "@app/configs";

export default () => {
  const [filter, setFilter] = React.useState({})
  const [isLoading, setLoading] = React.useState(false)
  const [userType, setUserType] = React.useState("users")
  //const [userType, setUserType] = React.useState("anonymous")
  const [userIds, setUserId] = React.useState([])
  const [searchValue, setSearch] = React.useState({key: "email", value: ""})
  const [isReloadTable, setReloadTable] = React.useState("")
  const [userInfo, setUserInfo] = React.useState(null)

  const formatMB = (num) => {
    return num !== 0 ? ((parseFloat(num || 0)) / (1024 * 1024)).toFixed(2) : 0
  }

  const onSearch = (value) => {
    if (value === "") {
      delay(() => {
        setReloadTable((new Date()).getTime().toString())
        setSearch({
          ...searchValue,
          value: ""
        })
      }, 300)
    } else {
      delay(() => {
        setSearch({
          ...searchValue,
          value
        })
      }, 300)
    }
  }

  const removeUsers = () => {
    Modal.confirm({
      title: `Are you sure want to delete users`,
      onOk: () => {
        setLoading(true)
        let delUser = []
        for (let i = 0; i < userIds.length; i++) {
          if (userIds?.[i]?.table_row_type === "users") {
            delUser.push(firestore.collection("users").doc(userIds?.[i]?.email).delete())
            delUser.push(postDataWithFetch(`${API}removeUser`, {
              "email": userIds?.[i]?.email
            }, {}))
          } else {
            delUser.push(firestore.collection("anonymous").doc(userIds?.[i]?.id).delete())
          }
        }
        Promise.all(delUser).then(() => {
          setLoading(false)
          notification.info({
            description: `User successfully deleted!`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification info",
            onClose: () => {
              setReloadTable((new Date()).getTime().toString())
              setUserId([])
            }
          });
        }).catch(() => {
          setLoading(false)
          notification.info({
            description: `Error removing user!`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification error",
            onClose: () => {

            }
          });
        });
      }
    })
  }

  React.useEffect(() => {
    setReloadTable((new Date()).getTime().toString())
  }, [userType])


  React.useEffect(() => {
    auth.onAuthStateChanged((user) => {
      if (user) {
        setUserInfo(user)
      }
    })
  }, [])

  const currentUser = auth?.currentUser
  const isAllow = (currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) || (userInfo?.email && userInfo?.email !== EMAIL)

  return (
    <Layout title="Server" description="desc of server">
      <div className="core-card">
        <div className="flex justify-between">
          {
            userIds.length === 0 ? (
                <div className="flex">
                  <UISearch
                    onChange={({target: {value}}) => onSearch(value)}
                    onKeyDown={(event) => {
                      const keyCode = event.which || event.keyCode;
                      const {value: search} = event.target;
                      if (keyCode === 13 && search) {
                        setSearch({
                          ...searchValue,
                          value: search
                        })
                      }
                    }}
                    placeholder="Search User Name, Email"/>
                  <div className="flex ml-10 items-center">
                    <div className="mr-3 pa-13 font-bold text-black">
                      Filter by:
                    </div>
                    <Select
                      value={filter?.status}
                      onChange={(e) => setFilter({...filter, status: e})}
                      placeholder="All Status" style={{width: 123}}>
                      <Select.Option value="">All Status</Select.Option>
                      <Select.Option value={true}>Enable</Select.Option>
                      <Select.Option value={false}>Disable</Select.Option>
                    </Select>
                    <div className="flex ml-3">
                      <Select
                        value={userType}
                        onChange={(e) => {
                          setUserType(e)
                        }}
                        placeholder="User Type" style={{width: 123}}>
                        <Select.Option value={"users"}>User</Select.Option>
                        <Select.Option value={"anonymous"}>Anonymous</Select.Option>
                      </Select>
                    </div>
                  </div>
                </div>
              )
              : isAllow && (
                <UIButton
                  onClick={isAllow && removeUsers}
                  className="third">
                  Remove user
                </UIButton>
              )
          }
        </div>

        <div className="mt-6">
          <UITable
            onSelectAll={(e, a) => {
              setUserId(a)
            }}
            customComp={{
              status: ({text, row}) => <Tag className="uppercase" style={{width: 110}}
                                            type={row?.premium ? 'primary' : 'second'}>{!row?.premium ? 'Free' : 'Premium'}</Tag>,
              action: ({row}) => (
                <div className="flex items-center justify-center">
                  <Dropdown
                    align="bottomRight"
                    overlayStyle={{width: 124}}
                    overlay={
                      isAllow
                        ? (
                          <Menu style={{borderRadius: 4}}>
                            <Menu.Item key="0">
                              <Link to={`/users/${row?.id}/${userType}`}>
                                Detail
                              </Link>
                            </Menu.Item>
                            <Menu.Item key="3" onClick={() => {
                              Modal.confirm({
                                title: `Are you sure want to delete ${row?.email} server`,
                                onOk: () => {
                                  setLoading(true)
                                  firestore.collection("users").doc(row?.id).delete().then(() => {
                                    if (userType === "users") {
                                      return postDataWithFetch(`${API}removeUser`, {
                                        "email": row?.id
                                      }, {})
                                    } else return Promise.resolve()
                                  })
                                    .then(() => {
                                      setLoading(false)
                                      notification.info({
                                        description: `User successfully deleted!`,
                                        placement: "bottomRight",
                                        duration: 2,
                                        icon: "",
                                        className: "core-notification info",
                                        onClose: () => {
                                          setReloadTable((new Date()).getTime().toString())
                                        }
                                      });
                                    })
                                    .catch(() => {
                                      setLoading(false)
                                      notification.info({
                                        description: `Error removing user!`,
                                        placement: "bottomRight",
                                        duration: 2,
                                        icon: "",
                                        className: "core-notification error",
                                      });
                                    });
                                }
                              })
                            }}>
                              Remove
                            </Menu.Item>
                          </Menu>
                        )
                        : (<span/>)
                    }
                    trigger={['click']}
                  >
                    <UIButton className="icon" style={{minWidth: 24}}>
                      <img src={MoreIcon} alt="" width={24} height={24}/>
                    </UIButton>
                  </Dropdown>
                </div>
              ),
              email: ({text, row}) => (
                <div className="lowercase text-left">
                  {
                    isAllow ?
                      (
                        <Link to={`/users/${row?.id}/${userType}`} className="base-text"
                              style={{color: "#2a2a2c"}}>
                          {text || row?.id}
                        </Link>
                      ) : (
                        <div className="base-text"
                             style={{color: "#2a2a2c"}}>
                          {encodeEmail(text || row?.id)}
                        </div>)
                  }

                </div>
              ),
              download: ({row}) => <div className="">{formatMB(row?.traffic?.download || 0)}</div>,
              upload: ({row}) => <div className="">{formatMB(row?.traffic?.upload || 0)}</div>,
              package: ({text}) => <div className="">{text || 'Free'}</div>,
            }}
            isReload={isReloadTable}
            service={userType}
            search={searchValue}
            isHiddenPg={false}
            defineCols={[
              {
                name: () => (
                  <div className="text-left flex items-center">
                    <span>Email</span>
                  </div>
                ),
                code: "email",
                sort: 1
              },
              {
                name: () => <div className="text-center">Status</div>,
                code: "status",
                sort: 2
              },
              {
                name: () => <div className="text-center">Package</div>,
                code: "package",
                sort: 3
              },
              {
                name: () => <div className="text-center">Download (MB)</div>,
                code: "download",
                sort: 4
              },
              {
                name: <div className="text-center">Upload (MB)</div>,
                code: "upload",
                sort: 5
              },
              {
                name: () => <div className="text-center">Action</div>,
                code: "action",
                sort: 'end'
              }
            ]}
            payload={filter}
            headerWidth={{
              email: 440,
              action: 92,
              upload: 220,
              download: 220,
              package: 152,
              status: 152,
            }}
            columns={[]}
          />
        </div>
        {isLoading && <LoadingPage2/>}
      </div>
    </Layout>
  )
}