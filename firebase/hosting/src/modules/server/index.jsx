import Layout from "@app/components/layout";
import React from "react";
import UISearch from "@app/components/core/input/search";
import {Checkbox, Dropdown, Menu, Modal, notification, Select} from "antd";
import UIButton from "@app/components/core/button";
import {PremiumServer} from "@app/modules/server/components/detail";
import UITable from "@app/components/core/table";
import Tag from "@app/components/core/tag";

import MoreIcon from '@app/resources/images/more.svg'
import {auth, firestore} from "@app/services/firebase";
import {delay} from "@app/utils";
import {EMAIL} from "@app/configs";

export default () => {
  const [filter, setFilter] = React.useState({})
  const [userInfo, setUserInfo] = React.useState({})
  const [searchValue, setSearch] = React.useState({key: "country", value: ""})
  const [isShowAddServer, setShowAddServe] = React.useState({
    type: "",
    data: undefined
  })
  const [isReloadTable, setReloadTable] = React.useState("")
  const showModalAddNewServer = (type = "PREMIUM", data = undefined) => setShowAddServe({type, data})

  const onCloseAddServe = () => setShowAddServe("")


  React.useEffect(() => {
    auth.onAuthStateChanged((user) => {
      if (user) {
        setUserInfo(user)
      }
    })
  }, [])

  const remove = (row) => {
    firestore.collection("Servers").doc(row?.id).delete().then(() => {
      notification.info({
        description: `${row?.country} successfully deleted!`,
        placement: "bottomRight",
        duration: 2,
        icon: "",
        className: "core-notification info",
        onClose: () => setReloadTable((new Date()).getTime().toString())
      });
    }).catch((error) => {
      notification.info({
        description: `${row?.country} failure deleted!`,
        placement: "bottomRight",
        duration: 2,
        icon: "",
        className: "core-notification error",
      });
    });
  }

  const search = (value) => {
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

  const currentUser = auth?.currentUser

  return (
    <Layout title="Server" description="desc of server">
      <div className="core-card">
        <div className="flex justify-between">
          <div className="flex">
            <UISearch
              onChange={({target: {value}}) => search(value)}
              placeholder="Search Server Name"/>
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
              <Select
                value={filter?.premium}
                onChange={(e) => setFilter({...filter, premium: e})}
                placeholder="Premium & Free"
                className="ml-3" style={{width: 155}}>
                <Select.Option value="">Premium & Free</Select.Option>
                <Select.Option value={true}>Premium</Select.Option>
                <Select.Option value={false}>Free</Select.Option>
              </Select>
            </div>
          </div>
          <div className="flex">
            {
              ((currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) || (userInfo?.email && userInfo?.email !== EMAIL)) && (
                <>

                </>
              )
            }
            <UIButton className="secondary mr-3" onClick={() => showModalAddNewServer("PREMIUM")}>New premium
              server</UIButton>
            <UIButton className="secondary" onClick={() => showModalAddNewServer("FREE")}
                      style={{background: 'var(--third-color)'}}>New free server</UIButton>
          </div>
        </div>

        <div className="mt-6">
          <UITable
            isAddParams={false}
            onSelectAll={(e) => console.log(e)}
            customComp={{
              status: ({text}) => <Tag className="uppercase"
                                       type={Boolean(text) ? 'primary' : 'second'}>{Boolean(text) ? 'Enable' : 'Disabled'}</Tag>,
              premium: ({text}) => <Tag className="uppercase"
                                        type={Boolean(text) ? 'primary' : 'second'}>{Boolean(text) ? 'Premium' : 'Free'}</Tag>,
              recommend: ({text}) => <div
                className="text-black pa-13 capitalize text-center">{Boolean(text) ? 'True' : 'False'}</div>,
              country: ({text, row: {countryCode}}) => (
                <div className="text-left flex items-center">
                  <img className="rounded-full mr-2" style={{border: "2px solid #f5f5f5"}}
                       src={`/flags/${countryCode}.svg`} width={24} height={24}/>
                  <span>{text}</span>
                </div>
              ),
              action: ({row}) => (
                <div className="flex items-center justify-center">
                  <Dropdown
                    align="bottomRight"
                    overlayStyle={{width: 124}}
                    overlay={((currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) || (userInfo?.email && userInfo?.email !== EMAIL)) ? (
                      <Menu style={{borderRadius: 4}}>
                        <Menu.Item key="0"
                                   onClick={() => showModalAddNewServer(row?.premium ? "PREMIUM" : "FREE", row)}>Edit</Menu.Item>
                        <Menu.Item key="3" onClick={() => {
                          Modal.confirm({
                            title: `Are you sure want to delete ${row?.country} server`,
                            onOk: () => remove(row)
                          })
                        }}>
                          Remove
                        </Menu.Item>
                      </Menu>
                    ) : (<span/>)}
                    trigger={['click']}>
                    <UIButton className="icon" style={{minWidth: 24}}>
                      <img src={MoreIcon} alt="" width={24} height={24}/>
                    </UIButton>
                  </Dropdown>
                </div>
              )
            }}
            isReload={isReloadTable}
            service={"Servers"}
            search={searchValue}
            isHiddenPg={false}
            defineCols={[
              {
                name: () => (
                  <div className="text-left flex items-center">
                    <span>Countries</span>
                  </div>
                ),
                code: "country",
                sort: 1
              },
              {
                name: () => <div className="text-center">State</div>,
                code: "state",
                sort: 2
              },
              {
                name: () => <div className="text-center">Status</div>,
                code: "status",
                sort: 3
              },
              {
                name: () => <div className="text-center">Premium</div>,
                code: "premium",
                sort: 4
              },
              {
                name: <div className="text-center">IP Address</div>,
                code: "ipAddress",
                sort: 5
              },
              {
                name: <div className="text-center">recommend</div>,
                code: "recommend",
                sort: 6
              },
              {
                name: () => <div className="text-center">Action</div>,
                code: "action",
                sort: 'end'
              }
            ]}
            payload={filter}
            headerWidth={{
              country: 535,
              action: 92,
              status: 90,
              premium: 90,
              recommend: 140,
              ipAddress: 139,
              state: 210
            }}
            columns={[]}
          />
        </div>
      </div>
      {
        isShowAddServer.type && (
          <PremiumServer
            cb={() => setReloadTable((new Date()).getTime().toString())}
            isPremium={isShowAddServer.type === "PREMIUM"}
            onCloseServe={onCloseAddServe}
            data={isShowAddServer.data}
          />
        )
      }
    </Layout>
  )
}